/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.browser.customtabs.CustomTabsIntent;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.pkcs11caller.Token;
import ru.rutoken.demobank.pkcs11caller.TokenManager;
import ru.rutoken.demobank.ui.ManagedActivity;
import ru.rutoken.demobank.ui.TokenManagerListener;
import ru.rutoken.demobank.ui.login.LoginActivity;
import ru.rutoken.demobank.utils.PcscChecker;
import ru.rutoken.demobank.utils.TokenModelRecognizer;

public class MainActivity extends ManagedActivity {
    // Vars
    public static final String EXTRA_TOKEN_SERIAL = "TOKEN_SERIAL";
    public static final String EXTRA_CERTIFICATE_FINGERPRINT = "CERTIFICATE_FINGERPRINT";
    public static final String IDENTIFIER = MainActivity.class.getName();

    private static final String PRIVACY_POLICY_URL = "https://www.rutoken.ru/company/policy/demobank-android.html";

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

    /**
     * Opens URL in a browser using Custom Tabs API
     */
    private static void launchCustomTabsUrl(Context context, Uri url) {
        new CustomTabsIntent.Builder().build().launchUrl(context, url);
    }

    @Override
    public String getActivityClassIdentifier() {
        return IDENTIFIER;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setupActionBar();
        setupUI();

        getLifecycle().addObserver(TokenManager.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toPrivacyPolicy) {
            launchCustomTabsUrl(this, Uri.parse(PRIVACY_POLICY_URL));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PcscChecker.checkPcscInstallation(this);
    }

    @Override
    public void onBackPressed() {
        if (TokenManagerListener.getInstance(this).shallWaitForToken()) {
            TokenManagerListener.getInstance(this).resetWaitForToken();
        } else {
            super.onBackPressed();
        }
        updateScreen();
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
                TokenManager.getInstance().removeNfcToken(TokenManagerListener.getInstance(this).getTokenSerial()));
        mRemoveNfcButton.setPaintFlags(mRemoveNfcButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public void startPINActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra(EXTRA_TOKEN_SERIAL, TokenManagerListener.getInstance(this).getTokenSerial());
        intent.putExtra(EXTRA_CERTIFICATE_FINGERPRINT,
                TokenManagerListener.getInstance(this).getCertificateFingerprint());
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
        updateScreenInfo(TokenManagerListener.getInstance(this).getToken());
        updateProgressBar();
    }

    private void updateProgressBar() {
        if (TokenManagerListener.getInstance(this).shallShowProgressBar()) {
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
        } else if (token != null && TokenManagerListener.getInstance(this)
                .getCertificateFingerprint().equals(TokenManagerListener.NO_FINGERPRINT)) {
            textToShow += getString(R.string.no_certificate);

            if (token.isNfc()) {
                mRemoveNfcButton.setText(R.string.continue_str);
                shallShowRemoveButton = true;
            }
        } else if (token != null) {
            textToShow += commonNameFromX500Name(
                    token.getCertificate(TokenManagerListener.getInstance(this).getCertificateFingerprint())
                            .getSubject());

            shallShowSelectButton = true;

            if (token.isNfc()) {
                mRemoveNfcButton.setText(R.string.disconnect);
                shallShowRemoveButton = true;
            }
        } else if (TokenManagerListener.getInstance(this).shallWaitForToken()) {
            textToShow = getString(R.string.wait_token,
                    TokenManagerListener.getInstance(this).getWaitToken().getShortDecSerialNumber());
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
