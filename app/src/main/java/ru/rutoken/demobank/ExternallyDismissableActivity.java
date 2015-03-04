package ru.rutoken.demobank;

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