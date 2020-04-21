/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ru.rutoken.demobank.payment.PaymentsActivity;
import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.pkcs11caller.TokenManager;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.utils.Pkcs11ErrorTranslator;

public class LoginActivity extends Pkcs11CallerActivity {
    /**
     * Data that we have received from the server to do a challenge-response authentication
     */
    private static final String SIGN_DATA = "sign me";

    // GUI
    private Button mLoginButton;
    private EditText mPinEditText;
    private TextView mAlertTextView;
    private ProgressBar mLoginProgressBar;

    protected String mTokenSerial = TokenManagerListener.NO_TOKEN;
    protected String mCertificateFingerprint = TokenManagerListener.NO_FINGERPRINT;
    protected Token mToken = null;

    private Dialog mOverlayDialog;

    @Override
    public String getActivityClassIdentifier() {
        return getClass().getName();
    }

    protected void showLogonStarted() {
        mLoginProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);
        mOverlayDialog.show();
    }

    protected void showLogonFinished() {
        mLoginProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(true);
        mOverlayDialog.dismiss();
    }

    @Override
    protected void manageLoginError(@Nullable Pkcs11Exception exception) {
        if (exception != null) {
            mAlertTextView.setText(Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode()));
        }
        showLogonFinished();
    }

    @Override
    protected void manageLoginSucceed() {
        // sign is used for a challenge-response authentication
        sign(mToken, mCertificateFingerprint, SIGN_DATA.getBytes());
    }

    @Override
    protected void manageSignError(@Nullable Pkcs11Exception exception) {
        String message = getString(R.string.error);
        if (exception != null) {
            message = Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode());
        }
        mToken.clearPin();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        showLogonFinished();
        Intent intent = new Intent(LoginActivity.this, PaymentsActivity.class);
        intent.putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial);
        intent.putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if ((displayMetrics.density > 1.0) && ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
            setContentView(R.layout.activity_login_high_density);
        } else {
            setContentView(R.layout.activity_login);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        setupUI();

        Intent intent = getIntent();
        mTokenSerial = intent.getStringExtra(MainActivity.EXTRA_TOKEN_SERIAL);
        mCertificateFingerprint = intent.getStringExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT);
        mToken = TokenManager.getInstance().tokenForId(mTokenSerial);
        if (null == mToken) {
            finish();
        }
        mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);
    }

    private void setupActionBar() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /* Custom actionbar */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(v, params);
        }
    }

    private void setupUI() {
        mLoginButton = findViewById(R.id.loginB);
        mPinEditText = findViewById(R.id.pinET);
        mAlertTextView = findViewById(R.id.alertTV);
        mLoginProgressBar = findViewById(R.id.loginPB);

        mLoginProgressBar.setVisibility(View.GONE);

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
        mPinEditText.requestFocus();

        mLoginButton.setOnClickListener(view -> {
            TokenManagerListener.getInstance().resetWaitForToken();
            showLogonStarted();
            login(mToken, mPinEditText.getText().toString());

        });
    }
}
