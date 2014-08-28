package ru.rutoken.demobank;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import ru.rutoken.Pkcs11Caller.TokenManager;

/**
 * Created by mironenko on 27.08.2014.
 */
public class DismissableActivity extends GuaranteedChildStartActivity {
    private static IntentFilter mFilter;
    static {
        mFilter = new IntentFilter();
        mFilter.addAction(MainActivity.DISMISS_ALL_ACTIVITIES);
    }
    private boolean mPendingDismiss = false;

    private final BroadcastReceiver mDismissReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(MainActivity.DISMISS_ALL_ACTIVITIES)) {
                if(DismissableActivity.this.hasPendingChildStart()) {
                    mPendingDismiss = true;
                } else {
                    DismissableActivity.this.finish();
                }
            }
        }
    };
    @Override
    protected void onChildCreated() {
        super.onChildCreated();
        if(mPendingDismiss) {
            Intent i =  new Intent(MainActivity.DISMISS_ALL_ACTIVITIES);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
            mPendingDismiss = false;
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mDismissReceiver, mFilter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mDismissReceiver);
    }
}