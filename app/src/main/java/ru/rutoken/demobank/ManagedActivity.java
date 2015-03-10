
package ru.rutoken.demobank;

abstract public class ManagedActivity extends ExternallyDismissableActivity {
    abstract String getActivityClassIdentifier();

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
