package ru.rutoken.demobank;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
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

public class PaymentsActivity extends Pkcs11CallerActivity {
    private LinearLayout mPaymentsLayout;
    private TextView mTokenModelTextView;
    private TextView mTokenIDTextView;
    private TextView mTokenBatteryTextView;
    private PopupWindow mPopupWindow;
    private AlertDialog mDialog;

    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    protected boolean mDoLoginAndSign = false;
    String mPin = null;
    private int mChecks;

    private byte mSignData[] = new byte[]{0,0,0};

    private static final String ACTIVITY_CLASS_IDENTIFIER = ExternallyDismissableActivity.class.getName();
    private String[] mPaymentTitles;
    private String[][] mPaymentArray = null;

    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }


    public static String PAYMENTS_CREATED = PaymentsActivity.class.getName() + "PAYMENTS_CREATED";
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
        createPayments();
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
        logout(mToken);
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
        // TODO
    }

    @Override
    protected void manageLogoutError() {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        onBackPressed();
        // TODO
    }

    @Override
    protected void manageLogoutSucceed() {
        if(mDoLoginAndSign) {
            mDoLoginAndSign = false;
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
                    showPaymentInfo((Payment)view);
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
        result += "<h2>" + getResources().getString(R.string.payment) + String.format("%d", num+Payment.FIRST_NUMBER)+"</h2>";
        result += "<font color=#CCCCCC>" + getResources().getString(R.string.fromDate) + " "+ date + "</font>";
        result += "<br/><br/>";
        for(int i = 0; i < mPaymentTitles.length; ++i) {
            result += "<font color=#CCCCCC>" + mPaymentTitles[i] + "</font><br/>";
            result += "<font color=#000000>" + mPaymentArray[num][i] + "</font><br/><br/>";
        }
        return result;
    }

    private void showPaymentInfoWithText(Spanned text, boolean bNeedAskPIN) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(true);

        mDialog = builder.create();
        View infoView = (LinearLayout) getLayoutInflater().inflate(R.layout.payment_info_layout, null);
        TextView dataTV = (TextView)infoView.findViewById(R.id.dataTV);
        dataTV.setText(text);
        mDialog.setView(infoView);

        final TextView paymentInfoTextView = (TextView) infoView.findViewById(R.id.dataTV);
        final Button sendButton = (Button) infoView.findViewById(R.id.sendB);
        final EditText signEditText = (EditText) infoView.findViewById(R.id.signET);
        final Button signButton = (Button) infoView.findViewById(R.id.signB);
        final ProgressBar progressBar = (ProgressBar)infoView.findViewById(R.id.progressBar);

        signButton.setVisibility(View.GONE);
        signEditText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if(bNeedAskPIN) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    paymentInfoTextView.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
                    signButton.setVisibility(View.VISIBLE);
                    signEditText.setVisibility(View.VISIBLE);
                }
            });
        } else {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    paymentInfoTextView.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    signAction();
                }
            });
        }

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signButton.setVisibility(View.GONE);
                signEditText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                mPin = signEditText.getText().toString();
                startLoginAndSignAction();
            }
        });

        mDialog.show();
    }

    private void showBatchPaymentInfo(int count, boolean bNeedAskPIN) {
        showPaymentInfoWithText(Html.fromHtml(String.format("Количество платежных поручений: %d.\nОтправить?", count)), bNeedAskPIN);
    }

    private void showPaymentInfo(Payment payment) {
        if(null == payment)  return;
        int number = payment.getNum();

        showPaymentInfoWithText(Html.fromHtml(createFullPaymentHtml(number)), needAskPIN(payment));
    }
}
