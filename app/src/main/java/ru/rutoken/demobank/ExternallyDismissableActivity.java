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
abstract public class ExternallyDismissableActivity extends GuaranteedChildStartActivity {
    abstract String getActivityClassIdentifier();

    private boolean mPendingDismiss = false;

    @Override
    protected void onChildCreated() {
        super.onChildCreated();
        if(mPendingDismiss) {
            mPendingDismiss = false;
            finish();
        }
    }

    protected void finishExternally() {
        if(hasPendingChildStart()) {
            mPendingDismiss = true;
        } else {
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        TokenManagerListener.getInstance().onActivityResumed(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        TokenManagerListener.getInstance().onActivityPaused(this);
    }
}