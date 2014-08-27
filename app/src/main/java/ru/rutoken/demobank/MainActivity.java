package ru.rutoken.demobank;

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.TextView;

import com.sun.jna.NativeLong;

import java.util.Set;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;
//import ru.rutoken.utils.TokenItem;

public class MainActivity extends TokenManagerListenerActivity {
    //GUI
    private TextView mInfoTextView;

    //Vars
    private boolean mPaymentsCreated = false;

    public static final NativeLong NO_SLOT = new NativeLong(-1);
    public static final NativeLong NO_CERTIFICATE = new NativeLong(0);
    public static final String NO_SERIAL = "NO_SERIAL";
    public static final String DISMISS_ALL_ACTIVITIES = MainActivity.class.getName() + "DISMISS_ALL_ACTIVITIES";

    protected NativeLong mSlotId = NO_SLOT;
    protected NativeLong mCertificate = NO_CERTIFICATE;
    protected String mSerial = NO_SERIAL;

    protected boolean mDoWait = false;
    protected NativeLong mWaitCertificate = NO_CERTIFICATE;
    protected String mWaitSerial = NO_SERIAL;

    protected int mTwbaCounter = 0;
    protected boolean mPendingDismiss = false;

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

    private final BroadcastReceiver mPaymentsCreatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(PaymentsActivity.PAYMENTS_CREATED)) {
                mPaymentsCreated = true;
            }
        }
    };

    protected void resetSlotInfo() {
        mSlotId = NO_SLOT;
        mCertificate = NO_CERTIFICATE;
        mSerial = NO_SERIAL;
    }

    protected void processConnectedToken(NativeLong slotId, Token token) {
        Set<NativeLong> certificates = null;
        String serial = null;
        if(null != token) {
            serial = token.getSerialNumber();
            certificates = token.enumerateCertificates();
            if(mDoWait) {
                if (certificates == null) return;
                if (!serial.equals(mWaitSerial)) return;
                boolean bFoundCertificate = false;
                for (NativeLong certificate: certificates) {
                    if (bFoundCertificate = (certificate.equals(mWaitCertificate)))
                        break;
                }
                if (bFoundCertificate) {
                    mDoWait = false;
                    mSlotId = slotId;
                    mSerial = mWaitSerial;
                    mCertificate = mWaitCertificate;

                    updateInfoLabel();
                    startPINActivity(); 
                }
            } else {
                if (mSlotId.equals(NO_SLOT)) {
                    mSlotId = slotId;
                    mSerial = serial;
                    if (certificates != null && certificates.iterator().hasNext()) {
                        mCertificate = certificates.iterator().next();
                    } else {
                        mCertificate = NO_CERTIFICATE;
                    }

                    updateInfoLabel();
                }
            }
        }
    }

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
        IntentFilter filter1 =  new IntentFilter();
        filter1.addAction(PaymentsActivity.PAYMENTS_CREATED);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mPaymentsCreatedReceiver, filter1);

        updateInfoLabel();
    }

    @Override
    public void onBackPressed() {
        if(mDoWait) {
            mDoWait = false;
        } else {
            super.onBackPressed();
        }
        updateInfoLabel();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBluetoothStateReciever);
        this.unregisterReceiver(mPaymentsCreatedReceiver);
    }

    private void setupUI() {
        mInfoTextView = (TextView)findViewById(R.id.infoTV);

        mInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startPINActivity();
            }
        });
    }

    private void startPINActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("slotId", mSlotId);
        intent.putExtra("serial", mSerial);
        intent.putExtra("certificate", mCertificate);
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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setBackgroundDrawable(
                    this.getResources().getDrawable(R.drawable.ab_bg));
            actionBar.setCustomView(v, params);
        }
    }



    @Override
    protected void onEnumerationFinished() {
        // TODO
    };
    @Override
    protected void onTokenWillBeAdded(Intent intent) {
        ++mTwbaCounter;
        if(mTwbaCounter>0) {
            // TODO show runner
        }
    };
    @Override
    protected void onTokenAddingFailed(Intent intent) {
        --mTwbaCounter;
        if(mTwbaCounter==0) {
            // TODO remove runner
        }

    };
    @Override
    protected void onTokenAdded(Intent intent) {
        --mTwbaCounter;
        if(mTwbaCounter==0) {
            // TODO remove runner
        }
        NativeLong slotId = (NativeLong)intent.getSerializableExtra("slotId");
        if (slotId == null) return;

        Token token = TokenManager.getInstance().tokenForSlot(slotId);
        processConnectedToken(slotId, token);
    };

    @Override
    protected void onTokenRemoved(Intent intent) {
        NativeLong slotId = (NativeLong)intent.getSerializableExtra("slotId");
        if (slotId == null) return;
        if (slotId.equals(mSlotId)) {
            if (mPaymentsCreated) {
                mPaymentsCreated = false;
                mDoWait = true;
                mWaitCertificate = mCertificate;
                mWaitSerial = mSerial;
            }
            resetSlotInfo();
            updateInfoLabel();
            for(NativeLong slot: TokenManager.getInstance().slots()) {
                if(!mSlotId.equals(NO_SLOT))
                    break;
                processConnectedToken(slot, TokenManager.getInstance().tokenForSlot(slot));
            }

            synchronized (MainActivity.this) {
                if(hasPendingChildStart()) {
                    mPendingDismiss = true;
                } else {
                    Intent i =  new Intent(DISMISS_ALL_ACTIVITIES);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                }
            }

        }
    };
    @Override
    protected void onInternalError() {
        // TODO show toast?
    };
    @Override
    protected void onChildCreated() {
        if(mPendingDismiss) {
            mPendingDismiss = false;
            Intent i =  new Intent(DISMISS_ALL_ACTIVITIES);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
        }
    }

    private void updateInfoLabel() {
        if (!mSerial.equals(NO_SERIAL) && !mCertificate.equals(NO_CERTIFICATE)) {
            // TODO -- show cert data
            mInfoTextView.setText("Токен сертифокен");
            mInfoTextView.setEnabled(true);
        } else if(!mSerial.equals(NO_SERIAL)) {
            mInfoTextView.setText("Токен шмокен");
            mInfoTextView.setEnabled(false);
        } else if(mDoWait) {
            mInfoTextView.setText("Токен ожидокен");
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