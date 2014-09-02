package ru.rutoken.demobank;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.jna.NativeLong;

import java.text.DateFormat;
import java.util.Date;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;
import ru.rutoken.utils.TokenBatteryCharge;
import ru.rutoken.utils.TokenModelRecognizer;

public class PaymentsActivity extends Pkcs11CallerActivity {
    private class InfoDialog {
        AlertDialog mDialog;
        Button mConfirmButton;
        TextView mPaymentTextView;
        InfoDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
            builder.setCancelable(true);
            mDialog = builder.create();
            View view = (LinearLayout) getLayoutInflater().inflate(R.layout.payment_info_layout, null);
            mDialog.setView(view);
            mConfirmButton = (Button) view.findViewById(R.id.sendB);
            mPaymentTextView = (TextView) view.findViewById(R.id.dataTV);
        }
        void show(Spanned text, boolean bNeedAskPIN) {
            mPaymentTextView.setText(text);

            if(bNeedAskPIN) {
                mConfirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                        mLoginDialog.show(null);
                    }
                });
            } else {
                mConfirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                        signAction();
                    }
                });
            }
            mDialog.show();
        }
        private void dismiss() {
            mDialog.dismiss();
        }
    }

    private class LoginDialog {
        AlertDialog mDialog;
        EditText mPinEditText;
        TextView mErrorTextView;
        String mPin;
        boolean mLogonBeingPerformed = false;
        LoginDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
            builder.setCancelable(true);

            mDialog = builder.create();
            View view = (LinearLayout) getLayoutInflater().inflate(R.layout.login_dialog, null);
            Button loginButton = (Button) view.findViewById(R.id.signB);
            mPinEditText = (EditText) view.findViewById(R.id.signET);
            mErrorTextView = (TextView) view.findViewById(R.id.errorTV);
            mDialog.setView(view);
            loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPin = mPinEditText.getText().toString();
                        mPinEditText.setText("");
                        mDialog.dismiss();
                        mLogonBeingPerformed = true;
                        startLoginAndSignAction();
                    }
                }
            );
        }
        String pin() {
            return mPin;
        }
        void show(String errorText) {
            if(mLogonBeingPerformed) {
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mLogonBeingPerformed = false;
                        onBackPressed();
                        //TODO -- go back to LoginActivity
                    }
                });
            } else {
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        mDialog.cancel();
                    }
                });
            }
            if(null != errorText) {
                mErrorTextView.setText(errorText);
            } else {
                mErrorTextView.setText("");
            }
            mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            mDialog.show();

        }
        void dismiss() {
            mDialog.dismiss();
        }

        public boolean isLogonBeingPerformed() {
            return mLogonBeingPerformed;
        }
        public void setLogonFinished() {
            mLogonBeingPerformed = false;
        }
    }

    // GUI
    private LinearLayout mPaymentsLayout;
    private TextView mTokenModelTextView;
    private TextView mTokenIDTextView;
    private TextView mTokenBatteryTextView;
    private ImageView mTokenBatteryImageView;
    private PopupWindow mPopupWindow;
    private InfoDialog mInfoDialog;
    private LoginDialog mLoginDialog;
    private AlertDialog mSucceedDialog;
    private AlertDialog mProgressDialog;

    private String[] mPaymentTitles;
    private String[][] mPaymentArray = null;
    //

    private static byte mSignData[] = new byte[]{0,0,0};

    // Activity input
    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    //

    // Logic
    private int mChecks;
    //

    // Support ExternallyDismissableActivity's abstract String getActivityClassIdentifier() method
    private static final String ACTIVITY_CLASS_IDENTIFIER = ExternallyDismissableActivity.class.getName();

    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        Intent intent = getIntent();
        mSlotId = (NativeLong)intent.getSerializableExtra("slotId");
        mCertificate = (NativeLong) intent.getSerializableExtra("certificate");
        mToken = TokenManager.getInstance().tokenForSlot(mSlotId);
        if(null == mToken) {
            finish();
        }
        TokenManagerListener.getInstance().setPaymentsCreated();
        setupUI();
    }

    private void setupActionBar() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /*Custom actionbar*/
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setBackgroundDrawable(
                    this.getResources().getDrawable(R.drawable.ab_bg));
            actionBar.setCustomView(v, params);
        }
    }

    private void setupUI() {
        mPaymentsLayout = (LinearLayout)findViewById(R.id.paymentsLayout);
        mTokenBatteryTextView = (TextView)findViewById(R.id.percentageTV);
        mTokenIDTextView = (TextView)findViewById(R.id.tokenIdTV);
        mTokenModelTextView = (TextView)findViewById(R.id.modelTV);
        mTokenBatteryImageView = (ImageView)findViewById(R.id.batteryIV);

        mTokenModelTextView.setText(TokenModelRecognizer.getInstance().marketingNameForPkcs11Name(mToken.getModel()));
        mTokenIDTextView.setText(mToken.getShortDecSerialNumber());

        int charge = TokenBatteryCharge.getBatteryPercentage(mToken.getCharge());
        mTokenBatteryTextView.setText(charge + "%");
        mTokenBatteryImageView.setImageResource(TokenBatteryCharge.getBatteryImageForVoltage(mToken.getCharge()));

        LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button popupButton = (Button)popupView.findViewById(R.id.popupB);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bNeedAskPIN = false;
                int paymentsCount = 0;
                for(int i = 0; i<mPaymentsLayout.getChildCount(); ++i) {
                    View childView = mPaymentsLayout.getChildAt(i);
                    if(Payment.class.isInstance(childView)) {
                        Payment payment = (Payment) childView;
                        CheckBox checkBox = (CheckBox)payment.findViewById(R.id.checkBox);
                        if(checkBox.isChecked()) {
                            bNeedAskPIN = bNeedAskPIN || payment.needAskPIN();
                            ++paymentsCount;
                        }
                    }
                }
                showBatchPaymentInfo(paymentsCount,bNeedAskPIN);
            }
        });

        createSucceedDialog();
        mInfoDialog = new InfoDialog();
        mLoginDialog = new LoginDialog();
        createProgressDialog();

        createPayments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TokenManagerListener.getInstance().resetPaymentsCreated();
        logout(mToken);
        super.onBackPressed();
    }

    @Override
    protected void manageLoginError(Pkcs11Exception exception) {
        mProgressDialog.dismiss();
        String message = null;
        if(exception != null) {
            message = exception.getMessage();
        }
        mLoginDialog.show(message);
        //TODO -- proper messages (localized)
    }

    @Override
    protected void manageLoginSucceed() {
        mLoginDialog.setLogonFinished();
        sign(mToken, mCertificate, mSignData);
        // proceed
    }

    @Override
    protected void manageSignError(Pkcs11Exception exception) {
        //Not sure this gonna happen
        // TODO -- process properly
        mProgressDialog.dismiss();
        String message = getResources().getString(R.string.error);
        if(exception != null) {
            message = exception.getMessage();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // TODO
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        mProgressDialog.dismiss();
        mSucceedDialog.show();
        // Go on, you're the boss
    }

    @Override
    protected void manageLogoutError(Pkcs11Exception exception) {
        if(mLoginDialog.isLogonBeingPerformed()) { // Well, anyway try to login, if sign process started
            login(mToken, mLoginDialog.pin());
        }
        // Nothing to do, if failed -- u seem to be quiting anyway
    }

    @Override
    protected void manageLogoutSucceed() {
        if(mLoginDialog.isLogonBeingPerformed()) {
            login(mToken, mLoginDialog.pin());
        }// Nothing to do -- u seem to be quiting anyway

    }

    private void createPayments() {
        Resources res = getResources();
        mPaymentTitles = res.getStringArray(R.array.payments_titles);
        TypedArray ta = res.obtainTypedArray(R.array.payments);
        int n = ta.length();
        mPaymentArray = new String[n][];
        for (int i = 0; i < n; ++i) {
            int id = ta.getResourceId(i, 0);
            if (id > 0) {
                mPaymentArray[i] = res.getStringArray(id);
            } else {
                // something wrong with the XML
            }
        }
        ta.recycle(); // Important!

        int nRecipient = 0;
        int nPrice = 0;
        for (int i = 0; i < mPaymentTitles.length; ++i) {
            if(mPaymentTitles[i].equals(res.getString(R.string.recepient))) {
                nRecipient = i;
            }
            if(mPaymentTitles[i].equals(res.getString(R.string.price))) {
                nPrice = i;
            }
        }

        for(int i = 0; i < mPaymentArray.length; ++i) {
            int price = Integer.valueOf(mPaymentArray[i][nPrice]);
            Payment payment = new Payment(this, null, i, mPaymentArray[i][nRecipient], price);
            payment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!Payment.class.isInstance(view)) return;
                    showOnePaymentInfo((Payment) view);
                }
            });
            CheckBox checkBox = (CheckBox)payment.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        ++mChecks;
                        mPopupWindow.showAtLocation(mPaymentsLayout, Gravity.BOTTOM, 0, 0);
                    } else {
                        --mChecks;
                        if (mChecks == 0) {
                            mPopupWindow.dismiss();
                        }
                    }
                }
            });
            mPaymentsLayout.addView(payment);
        }
    }

    protected void startLoginAndSignAction() {
        mProgressDialog.show();
        logout(mToken);
    }

    protected void signAction() {
        mProgressDialog.show();
        sign(mToken, mCertificate, mSignData);
    }

    protected String createFullPaymentHtml(int num) {
        String result = new String();
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new Date());
        result += "<h3>" + getResources().getString(R.string.payment) + String.format("%d", num+Payment.FIRST_NUMBER)+"</h3>";
        result += "<font color=#CCCCCC>" + getResources().getString(R.string.fromDate) + " "+ date + "</font>";
        result += "<br/><br/>";
        for(int i = 0; i < mPaymentTitles.length; ++i) {
            result += "<font color=#CCCCCC size=-1>" + mPaymentTitles[i] + "</font><br/>";
            result += "<font color=#000000 size=-1>" + mPaymentArray[num][i] + "</font><br/>";
        }
        return result;
    }

    private void showBatchPaymentInfo(int count, boolean bNeedAskPIN) {
        String message = String.format(getResources().getString(R.string.batch_sign_message), count);
        if(bNeedAskPIN) {
            message += "<br />"+getResources().getString(R.string.need_pin_message);
        } else {
            message += "<br />"+getResources().getString(R.string.batch_require_proceed);
        }
        mInfoDialog.show(Html.fromHtml(message), bNeedAskPIN);
    }

    private void showOnePaymentInfo(Payment payment) {
        if(null == payment)  return;
        int number = payment.getNum();

        mInfoDialog.show(Html.fromHtml(createFullPaymentHtml(number)), payment.needAskPIN());
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(false);
        mProgressDialog = builder.create();
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.progress_dialog, null);
        mProgressDialog.setView(view);
    }

    private void createSucceedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(true);
        mSucceedDialog = builder.create();
        View successView = (LinearLayout)getLayoutInflater().inflate(R.layout.result_dialog_layout, null);
        successView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSucceedDialog.dismiss();
            }
        });
        mSucceedDialog.setView(successView);
    }
}
