package ru.rutoken.demobank;

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.spongycastle.asn1.x500.AttributeTypeAndValue;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;

import java.util.Set;

import ru.rutoken.Pkcs11Caller.Certificate;
import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;
import ru.rutoken.utils.Pkcs11ErrorTranslator;
import ru.rutoken.utils.TokenModelRecognizer;
//import ru.rutoken.utils.TokenItem;

public class MainActivity extends ExternallyDismissableActivity {
    //GUI
    private TextView mInfoTextView;
    private ProgressBar mTWBAProgressBar;

    //Vars
    private static final String ACTIVITY_CLASS_IDENTIFIER = TokenManagerListener.MAIN_ACTIVITY_IDENTIFIER;

    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }


    private final BroadcastReceiver mBluetoothStateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        setupUI();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mBluetoothStateReciever, filter);
        TokenManagerListener.getInstance().init(getApplicationContext());
        TokenModelRecognizer.getInstance().init(getApplicationContext());
        Pkcs11ErrorTranslator.getInstance().init(getApplicationContext());

    }

    @Override
    public void onBackPressed() {
        if(TokenManagerListener.getInstance().shallWaitForToken()) {
            TokenManagerListener.getInstance().resetWaitForToken();
        } else {
            super.onBackPressed();
        }
        updateScreen();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mBluetoothStateReciever);
        if(isFinishing()) {
            TokenManagerListener.getInstance().destroy();
        }
    }

    private void setupUI() {
        mInfoTextView = (TextView)findViewById(R.id.infoTV);
        mTWBAProgressBar = (ProgressBar)findViewById(R.id.twbaPB);

        mTWBAProgressBar.setVisibility(View.INVISIBLE);

        mInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startPINActivity();
            }
        });
    }

    public void startPINActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("slotId", TokenManagerListener.getInstance().getSlotId());
        intent.putExtra("certificate", TokenManagerListener.getInstance().getCertificate());
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
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

    private static String commonNameFromX500Name(X500Name name) {
        String commonName = "";
        RDN[] rdns = null;
        rdns = name.getRDNs(BCStyle.CN);
        if(null == rdns || 0 == rdns.length)
            return commonName;
        commonName = IETFUtils.valueToString(rdns[0].getFirst().getValue());
        return commonName;
    }

    public void updateScreen() {
        updateInfoLabel();
        updateProgressBar();
    }

    private void updateProgressBar() {
        if(TokenManagerListener.getInstance().shallShowProgressBar()) {
            mTWBAProgressBar.setVisibility(View.VISIBLE);
        } else {
            mTWBAProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void updateInfoLabel() {
        String certificateData = null;
        Token token = TokenManagerListener.getInstance().getToken();

        if(token != null) {
            certificateData = new String();
            certificateData += TokenModelRecognizer.getInstance().marketingNameForPkcs11Name(token.getModel());
            certificateData += " ";
            certificateData += token.getShortDecSerialNumber();
            certificateData += "\n";
        }
        if (token != null && !TokenManagerListener.getInstance().getCertificate().equals(TokenManagerListener.NO_CERTIFICATE)) {
            certificateData += commonNameFromX500Name(token.getCertificate(TokenManagerListener.getInstance().getCertificate()).getSubject());
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(true);
        } else if(token != null) {
            certificateData += R.string.no_certificate;
            mInfoTextView.setText(certificateData);
            mInfoTextView.setEnabled(false);
        } else if(TokenManagerListener.getInstance().shallWaitForToken()) {
            Token waitToken = TokenManagerListener.getInstance().getWaitToken();
            X500Name subject = waitToken.getCertificate(TokenManagerListener.getInstance().getWaitCertificate()).getSubject();
            certificateData = String.format(getResources().getString(R.string.wait_token),
                    TokenModelRecognizer.getInstance().marketingNameForPkcs11Name(TokenManagerListener.getInstance().getWaitToken().getModel())
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
}