package ru.rutoken.demobank;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import ru.rutoken.Pkcs11Caller.TokenManager;

/**
 * Created by mironenko on 26.08.2014.
 */
public class TokenManagerListenerActivity extends Activity {
    private static IntentFilter tmFilter;
    static {
        tmFilter = new IntentFilter();
        tmFilter.addAction(TokenManager.ENUMERATION_FINISHED);
        tmFilter.addAction(TokenManager.TOKEN_WILL_BE_ADDED);
        tmFilter.addAction(TokenManager.TOKEN_ADDING_FAILED);
        tmFilter.addAction(TokenManager.TOKEN_WAS_ADDED);
        tmFilter.addAction(TokenManager.TOKEN_WAS_REMOVED);
        tmFilter.addAction(TokenManager.INTERNAL_ERROR);
    }
    private final BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(TokenManager.ENUMERATION_FINISHED)) {
                onEnumerationFinished();
            } else if (action.equals(TokenManager.TOKEN_WILL_BE_ADDED)) {
                onTokenWillBeAdded(intent);
            } else if (action.equals(TokenManager.TOKEN_ADDING_FAILED)) {
                onTokenAddingFailed(intent);
            } else if (action.equals(TokenManager.TOKEN_WAS_ADDED)) {
                onTokenAdded(intent);
            } else if (action.equals(TokenManager.TOKEN_WAS_REMOVED)) {
                onTokenRemoved(intent);
            } else if (action.equals(TokenManager.INTERNAL_ERROR)) {
                onInternalError();
            }
        }
    };

    protected void onEnumerationFinished() {

    };
    protected void onTokenWillBeAdded(Intent intent) {

    };
    protected void onTokenAddingFailed(Intent intent) {

    };
    protected void onTokenAdded(Intent intent) {

    };
    protected void onTokenRemoved(Intent intent) {

    };
    protected void onInternalError() {

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenManager.getInstance().init(getApplicationContext());
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mTokenReceiver, tmFilter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mTokenReceiver);
        if(isFinishing()) {
            TokenManager.getInstance().destroy();
        }
    }
}
