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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sun.jna.NativeLong;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;

public class PaymentsActivity extends Pkcs11CallerActivity {
    private Payment mBashneftView;
    private Payment mLukoilView;

    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    protected boolean mDoLoginAndSign = false;
    String mPin = null;

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
        setupUI();
        Intent intent = getIntent();
        mSlotId = (NativeLong)intent.getSerializableExtra("slotId");
        mCertificate = (NativeLong) intent.getSerializableExtra("certificate");
        mToken = TokenManager.getInstance().tokenForSlot(mSlotId);
        if(null == mToken) {
            finish();
        }
        TokenManagerListener.getInstance().setPaymentsCreated();

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
        mBashneftView = (Payment)findViewById(R.id.basneftPayment);
        mLukoilView = (Payment)findViewById(R.id.lukoilPayment);

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

    private void createPayments() {
        String[] data = getResources().getStringArray(R.array.bashneft_payment);
        mBashneftView.setDate(data[0]);
        mBashneftView.setReciever(data[1]);
        mBashneftView.setAmount(data[2]);

        data = getResources().getStringArray(R.array.lukoil_payment);
        mLukoilView.setDate(data[0]);
        mLukoilView.setReciever(data[1]);
        mLukoilView.setAmount(data[2]);
        mLukoilView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentInfo();
            }
        });
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
