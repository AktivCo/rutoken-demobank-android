/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

abstract public class ManagedActivity extends ExternallyDismissableActivity {
    public abstract String getActivityClassIdentifier();

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
