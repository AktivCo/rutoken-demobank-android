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
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;
import ru.rutoken.utils.TokenBatteryCharge;

public class PaymentsActivity extends Pkcs11CallerActivity {
    // GUI
    private LinearLayout mPaymentsLayout;
    private TextView mTokenModelTextView;
    private TextView mTokenIDTextView;
    private TextView mTokenBatteryTextView;
    private ImageView mTokenBatteryImageView;
    private PopupWindow mPopupWindow;
    private AlertDialog mInfoDialog;
    private AlertDialog mSucceedDialog;
    private ProgressDialog mProgressDialog;
    private AlertDialog mLogin;

    private String[] mPaymentTitles;
    private String[][] mPaymentArray = null;
    private Map<String,String> mModelNames = new HashMap<String,String>();
    //

    private static byte mSignData[] = new byte[]{0,0,0};

    // Activity input
    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    //

    // Logic
    protected boolean mDoLoginAndSign = false;
    protected boolean mGotLogoutError = false;
    String mPin = null;
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
        fillInModelNames();
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
            actionBar.setDisplayHomeAsUpEnabled(true);
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

        String marketingModelName = mModelNames.get(mToken.getModel());
        if(null == marketingModelName) {
            marketingModelName = "Unsupported Model";
        }
        mTokenModelTextView.setText(marketingModelName);
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
                            bNeedAskPIN = bNeedAskPIN || needAskPIN(payment);
                            ++paymentsCount;
                        }
                    }
                }
                showBatchPaymentInfo(paymentsCount,bNeedAskPIN);
            }
        });

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

        createPayments();
    }

    protected void fillInModelNames() {
        Resources res = getResources();
        if(null == res) {
            return;
        }
        String pkcs11Models[] = res.getStringArray(R.array.pkcs11_names);
        String marketingModels[] = res.getStringArray(R.array.marketing_names);
        assert(pkcs11Models.length == marketingModels.length);
        for (int i=0; i<pkcs11Models.length ; ++i) {
            mModelNames.put(pkcs11Models[i], marketingModels[i]);
        }
    }

    protected boolean needAskPIN(Payment payment) {
        return payment.getAmount() >= Payment.THRESHOLD_PRICE;
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
        if(!mGotLogoutError) {
            logout(mToken);
        } else {
            mGotLogoutError = false;
        }
        super.onBackPressed();
    }

    @Override
    protected void manageLoginError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();

        // showLogonFinished();
        //TODO
    }

    @Override
    protected void manageLoginSucceed() {
        if(mDoLoginAndSign){
            mDoLoginAndSign = false;
        }
        sign(mToken, mCertificate, mSignData);
    }

    @Override
    protected void manageSignError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        //logout(mToken);
        // TODO
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        if (mInfoDialog != null) {
            mInfoDialog.dismiss();
        }
        mSucceedDialog.show();
    }

    @Override
    protected void manageLogoutError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        mGotLogoutError = true;
        onBackPressed();
        // TODO
    }

    @Override
    protected void manageLogoutSucceed() {
        if(mDoLoginAndSign) {
            login(mToken, mPin);
        }
    }

    @Override
    protected void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        // TODO
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
        mDoLoginAndSign = true;
        logout(mToken);
    }

    protected void signAction() {
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

    private void showPaymentInfoWithText(Spanned text, boolean bNeedAskPIN) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(true);

        mInfoDialog = builder.create();
        View infoView = (LinearLayout) getLayoutInflater().inflate(R.layout.payment_info_layout, null);
        TextView dataTV = (TextView)infoView.findViewById(R.id.dataTV);
        dataTV.setText(text);
        mInfoDialog.setView(infoView);

        final TextView paymentInfoTextView = (TextView) infoView.findViewById(R.id.dataTV);
        final Button sendButton = (Button) infoView.findViewById(R.id.sendB);
        final EditText signEditText = (EditText) infoView.findViewById(R.id.signET);
        final Button signButton = (Button) infoView.findViewById(R.id.signB);
        final ProgressBar progressBar = (ProgressBar) infoView.findViewById(R.id.progressBar);
        final TextView errorTV = (TextView)infoView.findViewById(R.id.errorTV);

        signButton.setVisibility(View.GONE);
        signEditText.setVisibility(View.GONE);
        errorTV.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if(bNeedAskPIN) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    paymentInfoTextView.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
                    signButton.setVisibility(View.VISIBLE);
                    signEditText.setVisibility(View.VISIBLE);
                    errorTV.setVisibility(View.VISIBLE);
                }
            });
        } else {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    paymentInfoTextView.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    mInfoDialog.setCancelable(false);
                    signAction();
                }
            });
        }

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signButton.setVisibility(View.GONE);
                signEditText.setVisibility(View.GONE);
                errorTV.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                mPin = signEditText.getText().toString();
                mInfoDialog.setCancelable(false);
                startLoginAndSignAction();
            }
        });

        mInfoDialog.show();
    }

    private void showBatchPaymentInfo(int count, boolean bNeedAskPIN) {
        String message = String.format(getResources().getString(R.string.batch_sign_message), count);
        if(bNeedAskPIN) {
            message += "<br />"+getResources().getString(R.string.need_pin_message);
        } else {
            message += "<br />"+getResources().getString(R.string.batch_require_proceed);
        }
        showPaymentInfoWithText(Html.fromHtml(message), bNeedAskPIN);
    }

    private void showOnePaymentInfo(Payment payment) {
        if(null == payment)  return;
        int number = payment.getNum();

        showPaymentInfoWithText(Html.fromHtml(createFullPaymentHtml(number)), needAskPIN(payment));
    }
}
