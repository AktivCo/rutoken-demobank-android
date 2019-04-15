/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;

import java.util.Objects;

import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.utils.PcscChecker;
import ru.rutoken.utils.TokenModelRecognizer;

public class MainActivity extends ManagedActivity {
    // Vars
    private static final String ACTIVITY_CLASS_IDENTIFIER = TokenManagerListener.MAIN_ACTIVITY_IDENTIFIER;
    // GUI
    private TextView mInfoTextView;
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        mInfoTextView.setText(R.string.turn_bt_on);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mInfoTextView.setText(R.string.no_token);
                        break;
                }
            }
        }
    };
    private ProgressBar mTWBAProgressBar;

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
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBluetoothStateReceiver, filter);
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
        unregisterReceiver(mBluetoothStateReceiver);
        if (isFinishing()) {
            TokenManagerListener.getInstance().destroy();
        }
    }

    private void setupUI() {
        mInfoTextView = findViewById(R.id.infoTV);
        mTWBAProgressBar = findViewById(R.id.twbaPB);

        mTWBAProgressBar.setVisibility(View.INVISIBLE);

        mInfoTextView.setOnClickListener(view -> MainActivity.this.startPINActivity());
    }

    public void startPINActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("slotId", TokenManagerListener.getInstance().getSlotId());
        intent.putExtra("certificate", TokenManagerListener.getInstance().getCertificate());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
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
            actionBar.setBackgroundDrawable(getDrawable(R.drawable.ab_bg));
            actionBar.setCustomView(v, params);
        }
    }

    public void updateScreen(Token token) {
        updateInfoLabel(token);
        updateProgressBar();
    }

    public void updateScreen() {
        updateScreen(TokenManagerListener.getInstance().getToken());
    }

    private void updateProgressBar() {
        if (TokenManagerListener.getInstance().shallShowProgressBar()) {
            mTWBAProgressBar.setVisibility(View.VISIBLE);
        } else {
            mTWBAProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateInfoLabel(Token token) {
        String certificateData = getCertificateData(token);

        if (token != null && token.smInitializedStatus() == Token.SmInitializedStatus.NEED_INITIALIZE) {
            certificateData += getString(R.string.need_sm_activate);
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(false);
        } else if (token != null && TokenManagerListener.getInstance().getCertificate().equals(TokenManagerListener.NO_CERTIFICATE)) {
            certificateData += getString(R.string.no_certificate);
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(false);
        } else if (token != null && TokenManagerListener.getInstance().getCertificate().equals(TokenManagerListener.MORE_THAN_ONE_CERTIFICATE)) {
            certificateData += getString(R.string.more_than_one_certificate);
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(false);
        } else if (token != null) {
            certificateData += commonNameFromX500Name(token.getCertificate(TokenManagerListener.getInstance().getCertificate()).getSubject());
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(true);
        } else if (TokenManagerListener.getInstance().shallWaitForToken()) {
            certificateData = String.format(getString(R.string.wait_token),
                    TokenModelRecognizer.getInstance(this).marketingNameForPkcs11Name(TokenManagerListener.getInstance().getWaitToken().getModel())
                            + " " + TokenManagerListener.getInstance().getWaitToken().getShortDecSerialNumber());
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(false);
        } else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            mInfoTextView.setText(R.string.turn_bt_on);
            mInfoTextView.setEnabled(false);
        } else {
            mInfoTextView.setText(R.string.no_token);
            mInfoTextView.setEnabled(false);
        }
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
