/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Objects;

/**
 * Activity calls onChildCreated method when child activity is started. Beware to use startActivity
 * to start child activity. All methods are processed in the MainThread -- no synchronization
 * needed.
 */

abstract class GuaranteedChildStartActivity extends AppCompatActivity {
    private static final IntentFilter mFilter;
    private static final String CHILD_ACTIVITY_CREATED =
            GuaranteedChildStartActivity.class.getName() + ".CHILD_ACTIVITY_CREATED";

    static {
        mFilter = new IntentFilter();
        mFilter.addAction(CHILD_ACTIVITY_CREATED);
    }

    private int mHashCode;
    private boolean mPendingActivityStart = false;
    private final BroadcastReceiver mChildActivityCreatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), CHILD_ACTIVITY_CREATED)) {
                int hashCode = intent.getIntExtra("hashCode", 0);
                if (mHashCode == hashCode) {
                    synchronized (GuaranteedChildStartActivity.this) {
                        mPendingActivityStart = false;
                        onChildCreated();
                    }
                }
            }
        }
    };
    private Integer mParentHashCode = null;

    protected boolean hasPendingChildStart() {
        return mPendingActivityStart;
    }

    protected void onChildCreated() {
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra("hashCode", mHashCode);
        mPendingActivityStart = true;
        super.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getApplicationContext())
                .registerReceiver(mChildActivityCreatedReceiver, mFilter);
        mHashCode = System.identityHashCode(this);
        Intent i = getIntent();
        int parentHashCode = i.getIntExtra("hashCode", 0);
        if (parentHashCode != 0) {
            mParentHashCode = parentHashCode;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mParentHashCode) {
            int parentHashCode = mParentHashCode;
            mParentHashCode = null;
            Intent intent = new Intent(CHILD_ACTIVITY_CREATED);
            intent.putExtra("hashCode", parentHashCode);
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this.getApplicationContext())
                .unregisterReceiver(mChildActivityCreatedReceiver);
    }
}
