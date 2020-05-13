/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.login;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.pkcs11caller.Token;
import ru.rutoken.demobank.pkcs11caller.TokenManager;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.demobank.ui.Pkcs11CallerActivity;
import ru.rutoken.demobank.ui.TokenManagerListener;
import ru.rutoken.demobank.ui.main.MainActivity;
import ru.rutoken.demobank.ui.nfc.NfcDetectCardFragment;
import ru.rutoken.demobank.ui.payment.PaymentsActivity;
import ru.rutoken.demobank.utils.Pkcs11ErrorTranslator;

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
    private Dialog mOverlayDialog;

    private String mTokenSerial = TokenManagerListener.NO_TOKEN;
    private String mCertificateFingerprint = TokenManagerListener.NO_FINGERPRINT;
    private Token mToken = null;

    @Override
    public String getActivityClassIdentifier() {
        return getClass().getName();
    }

    private void showLogonStarted() {
        mLoginProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);
        mOverlayDialog.show();
    }

    private void showLogonFinished() {
        mLoginProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(true);
        mOverlayDialog.dismiss();
    }

    @Override
    protected void manageTokenOperationError(@Nullable Pkcs11Exception exception) {
        mToken.clearPin();
        String message = (exception == null) ? getString(R.string.error)
                : Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode());

        mAlertTextView.setText(message);
        showLogonFinished();
    }

    @Override
    protected void manageTokenOperationCanceled() {
        showLogonFinished();
    }

    @Override
    protected void manageTokenOperationSucceed() {
        showLogonFinished();

        startActivity(new Intent(LoginActivity.this, PaymentsActivity.class)
                .putExtra(MainActivity.EXTRA_TOKEN_SERIAL, mTokenSerial)
                .putExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT, mCertificateFingerprint));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        Intent intent = getIntent();
        mTokenSerial = intent.getStringExtra(MainActivity.EXTRA_TOKEN_SERIAL);
        mCertificateFingerprint = intent.getStringExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT);
        mToken = TokenManager.getInstance().getTokenBySerial(mTokenSerial);
        if (mToken == null) {
            Toast.makeText(this, R.string.rutoken_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mOverlayDialog = new Dialog(this, android.R.style.Theme_Panel);
        mOverlayDialog.setCancelable(false);

        setupActionBar();
        setupUI();
    }

    private void setupActionBar() {
        View view = getLayoutInflater().inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /* Custom actionbar */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view, params);
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
            TokenManagerListener.getInstance(this).resetWaitForToken();
            showLogonStarted();

            // Certificate and sign data are used for a challenge-response authentication.
            login(mToken, mPinEditText.getText().toString(), mCertificateFingerprint, SIGN_DATA.getBytes());

        });
    }
}
