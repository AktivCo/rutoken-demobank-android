package ru.rutoken.demobank;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import android.widget.TextView;

import com.sun.jna.NativeLong;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;

public class PaymentsActivity extends Pkcs11CallerActivity {
    private LinearLayout mPaymentsLayout;
    private TextView mTokenModelTextView;
    private TextView mTokenIDTextView;
    private TextView mTokenBatteryTextView;
    private PopupWindow mPopupWindow;

    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    protected boolean mDoLoginAndSign = false;
    String mPin = null;
    private int mChecks;

    private byte mSignData[] = new byte[]{0,0,0};

    private static final String ACTIVITY_CLASS_IDENTIFIER = ExternallyDismissableActivity.class.getName();

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

        int[] IDs = new int[2];
        IDs[0] = R.array.bashneft_payment;
        IDs[1] = R.array.lukoil_payment;
        createPayments(IDs);
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
        // showLogonFinished();
        //TODO
    }

    @Override
    protected void manageLoginSucceed() {
        sign(mToken, mCertificate, mSignData);
    }

    @Override
    protected void manageSignError() {
        logout(mToken);
        // TODO
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        // TODO
    }

    @Override
    protected void manageLogoutError() {
        // showLogonFinished();
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
        //mAlertTextView.setText(error);
        // TODO
    }

    private void createPayments(int[] IDs) {
        for (int i = 0; i < IDs.length; ++i) {
            Payment payment = new Payment(PaymentsActivity.this);

            String[] data = getResources().getStringArray(IDs[i]);
            payment.setNum(data[0]);
            payment.setDate(data[1]);
            payment.setReciever(data[2]);
            payment.setAmount(data[3]);

            mPaymentsLayout.addView(payment);
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
        }
    }

    protected void startLoginAndSignAction() {
        mDoLoginAndSign = true;
        logout(mToken);
    }

    protected void signAction() {
        sign(mToken, mCertificate, mSignData);
    }

    private void showPaymentInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        View infoView = (LinearLayout) getLayoutInflater().inflate(R.layout.payment_info_layout, null);
        dialog.setView(infoView);

        final TextView paymentInfoTextView = (TextView) infoView.findViewById(R.id.dataTV);
        final Button sendButton = (Button) infoView.findViewById(R.id.sendB);
        final EditText signEditText = (EditText) infoView.findViewById(R.id.signET);
        final Button signButton = (Button) infoView.findViewById(R.id.signB);

        signButton.setVisibility(View.GONE);
        signEditText.setVisibility(View.GONE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentInfoTextView.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
                signAction();
                if(false) { // TODO -- check price
                    signButton.setVisibility(View.VISIBLE);
                    signEditText.setVisibility(View.VISIBLE);
                }
            }
        });

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signButton.setVisibility(View.GONE);
                signEditText.setVisibility(View.GONE);
                startLoginAndSignAction();
            }
        });

        dialog.show();
    }
}
