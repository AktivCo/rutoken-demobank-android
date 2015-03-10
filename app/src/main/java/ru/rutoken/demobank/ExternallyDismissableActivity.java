
package ru.rutoken.demobank;

abstract public class ExternallyDismissableActivity extends GuaranteedChildStartActivity {
    private boolean mPendingDismiss = false;

    @Override
    protected void onChildCreated() {
        super.onChildCreated();
        if (mPendingDismiss) {
            mPendingDismiss = false;
            finish();
        }
    }

    protected void finishExternally() {
        if (hasPendingChildStart()) {
            mPendingDismiss = true;
        } else {
            finish();
        }
    }
}
