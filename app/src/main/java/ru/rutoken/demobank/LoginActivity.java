package ru.rutoken.demobank;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sun.jna.NativeLong;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;


public class LoginActivity extends Pkcs11CallerActivity {
    //GUI
    private Button mLoginButton;
    private EditText mPinEditText;
    private TextView mAlertTextView;
    private ProgressBar mLoginProgressBar;

    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    private final static String hardcodedPIN = "12345678";
    private static final byte mSignData[] = new byte[]{0,0,0};
    private static final String ACTIVITY_CLASS_IDENTIFIER = LoginActivity.class.getName();

    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }

    protected void showLogonStarted() {
        mLoginProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);
    }

    protected void showLogonFinished() {
        mLoginProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(true);
    }


    @Override
    protected void manageLoginError(Pkcs11Exception exception) {
        if(exception != null) {
            mAlertTextView.setText(exception.getMessage());
        }
        showLogonFinished();
        //TODO
    }

    @Override
    protected void manageLoginSucceed() {
        sign(mToken, mCertificate, mSignData);
    }

    @Override
    protected void manageSignError(Pkcs11Exception exception) {
        logout(mToken);
        // TODO
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        Intent intent = new Intent(LoginActivity.this, PaymentsActivity.class);
        intent.putExtra("slotId", mSlotId);
        intent.putExtra("certificate", mCertificate);
        startActivity(intent);
    }

    @Override
    protected void manageLogoutError(Pkcs11Exception exception) {
        showLogonFinished();
        //TODO
    }

    @Override
    protected void manageLogoutSucceed() {
        showLogonFinished();
        //TODO
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
    }

    private void setupActionBar() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        mLoginButton = (Button)findViewById(R.id.loginB);
        mPinEditText = (EditText)findViewById(R.id.pinET);
        mAlertTextView = (TextView)findViewById(R.id.alertTV);
        mLoginProgressBar = (ProgressBar)findViewById(R.id.loginPB);

        mLoginProgressBar.setVisibility(View.GONE);

        mLoginButton.setBackgroundColor(Color.TRANSPARENT);
        mLoginButton.setEnabled(false);

        mPinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (mPinEditText.getText().toString().isEmpty()) {
                    mLoginButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mPinEditText.getText().toString().isEmpty()) {
                    mLoginButton.setEnabled(false);
                } else {
                    mLoginButton.setEnabled(true);
                }
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TokenManagerListener.getInstance().resetWaitForToken();
                showLogonStarted();
                login(mToken, mPinEditText.getText().toString());

            }
        });
    }
}
