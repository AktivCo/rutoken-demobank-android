package ru.rutoken.demobank;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * All methods are processed in the MainThread -- no synchronization needed.
 */

public class GuaranteedChildStartActivity extends Activity {
    private static IntentFilter mFilter;
    private int mHashCode;
    private boolean mPendingActivityStart = false;
    private Integer mParentHashCode = null;
    public static final String CHILD_ACTIVITY_CREATED = GuaranteedChildStartActivity.class.getName() + ".CHILD_ACTIVITY_CREATED";

    static {
        mFilter = new IntentFilter();
        mFilter.addAction(CHILD_ACTIVITY_CREATED);
    }

    private final BroadcastReceiver mChildActivityCreatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(CHILD_ACTIVITY_CREATED)) {
                int hashCode = intent.getIntExtra("hashCode", 0);
                if(mHashCode == hashCode) {
                    synchronized (GuaranteedChildStartActivity.this) {
                        mPendingActivityStart = false;
                        onChildCreated();
                    }
                }
            }
        }
    };

    protected boolean hasPendingChildStart() {
        return mPendingActivityStart;
    }

    protected void onChildCreated() {}

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra("hashCode", mHashCode);
        mPendingActivityStart = true;
        super.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).registerReceiver(mChildActivityCreatedReceiver, mFilter);
        mHashCode = System.identityHashCode(this);
        Intent i = getIntent();
        int parentHashCode = i.getIntExtra("hashCode", 0);
        if(parentHashCode != 0) {
            mParentHashCode = parentHashCode;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null != mParentHashCode) {
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
        LocalBroadcastManager.getInstance(this.getApplicationContext()).unregisterReceiver(mChildActivityCreatedReceiver);
    }
}
