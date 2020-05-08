/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.pkcs11caller.TokenManager;
import ru.rutoken.utils.PcscChecker;
import ru.rutoken.utils.TokenModelRecognizer;

public class MainActivity extends ManagedActivity {
    // Vars
    public static final String EXTRA_TOKEN_SERIAL = "TOKEN_SERIAL";
    public static final String EXTRA_CERTIFICATE_FINGERPRINT = "CERTIFICATE_FINGERPRINT";
    private static final String ACTIVITY_CLASS_IDENTIFIER = TokenManagerListener.MAIN_ACTIVITY_IDENTIFIER;

    // GUI
    private TextView mInfoTextView;
    private ProgressBar mTWBAProgressBar;
    private Button mSelectButton;
    private Button mRemoveNfcButton;

    private static String commonNameFromX500Name(X500Name name) {
        String commonName = "";
        RDN[] rdns = name.getRDNs(BCStyle.CN);
        if (rdns == null || rdns.length == 0)
            return commonName;
        commonName = IETFUtils.valueToString(rdns[0].getFirst().getValue());
        return commonName;
    }

    @Override
    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        setupUI();
        TokenManagerListener.getInstance().init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        PcscChecker.checkPcscInstallation(this);
    }

    @Override
    public void onBackPressed() {
        if (TokenManagerListener.getInstance().shallWaitForToken()) {
            TokenManagerListener.getInstance().resetWaitForToken();
        } else {
            super.onBackPressed();
        }
        updateScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            TokenManagerListener.getInstance().destroy();
        }
    }

    private void setupUI() {
        mInfoTextView = findViewById(R.id.infoTV);
        mTWBAProgressBar = findViewById(R.id.tokenAddingProgressBar);
        mSelectButton = findViewById(R.id.selectButton);
        mRemoveNfcButton = findViewById(R.id.removeNfcButton);

        mTWBAProgressBar.setVisibility(View.INVISIBLE);

        mSelectButton.setVisibility(View.GONE);
        mSelectButton.setOnClickListener(view -> MainActivity.this.startPINActivity());
        mRemoveNfcButton.setOnClickListener(view ->
                TokenManager.getInstance().removeNfcToken(TokenManagerListener.getInstance().getTokenSerial()));
        mRemoveNfcButton.setPaintFlags(mRemoveNfcButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public void startPINActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(EXTRA_TOKEN_SERIAL, TokenManagerListener.getInstance().getTokenSerial());
        intent.putExtra(EXTRA_CERTIFICATE_FINGERPRINT, TokenManagerListener.getInstance().getCertificateFingerprint());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
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

    public void updateScreen() {
        updateScreenInfo(TokenManagerListener.getInstance().getToken());
        updateProgressBar();
    }

    private void updateProgressBar() {
        if (TokenManagerListener.getInstance().shallShowProgressBar()) {
            mTWBAProgressBar.setVisibility(View.VISIBLE);
        } else {
            mTWBAProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateScreenInfo(Token token) {
        String textToShow = getCertificateData(token);
        boolean shallShowSelectButton = false;
        boolean shallShowRemoveButton = false;

        if (token != null && token.smInitializedStatus() == Token.SmInitializedStatus.NEED_INITIALIZE) {
            textToShow += getString(R.string.need_sm_activate);
        } else if (token != null && TokenManagerListener.getInstance().getCertificateFingerprint().equals(TokenManagerListener.NO_FINGERPRINT)) {
            textToShow += getString(R.string.no_certificate);

            if (token.isNfc()) {
                mRemoveNfcButton.setText(R.string.continue_str);
                shallShowRemoveButton = true;
            }
        } else if (token != null) {
            textToShow += commonNameFromX500Name(token.getCertificate(TokenManagerListener.getInstance().getCertificateFingerprint())
                    .getSubject());

            shallShowSelectButton = true;

            if (token.isNfc()) {
                mRemoveNfcButton.setText(R.string.disconnect);
                shallShowRemoveButton = true;
            }
        } else if (TokenManagerListener.getInstance().shallWaitForToken()) {
            textToShow = getString(R.string.wait_token,
                    TokenManagerListener.getInstance().getWaitToken().getShortDecSerialNumber());
        } else {
            textToShow = getString(R.string.no_token);
            mRemoveNfcButton.setVisibility(View.GONE);
        }

        mInfoTextView.setText(textToShow);
        mSelectButton.setVisibility(shallShowSelectButton ? View.VISIBLE : View.GONE);
        mRemoveNfcButton.setVisibility(shallShowRemoveButton ? View.VISIBLE : View.GONE);
    }

    private String getCertificateData(@Nullable Token token) {
        StringBuilder certificateData = new StringBuilder();
        if (token != null) {
            certificateData.append(TokenModelRecognizer.getInstance(this).marketingNameForPkcs11Name(token.getModel()));
            certificateData.append(" ");
            certificateData.append(token.getShortDecSerialNumber());
            certificateData.append("\n");
        }
        return certificateData.toString();
    }
}
