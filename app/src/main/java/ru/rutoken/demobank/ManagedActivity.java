/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

abstract public class ManagedActivity extends ExternallyDismissibleActivity {
    public abstract String getActivityClassIdentifier();

    @Override
    protected void onResume() {
        super.onResume();
        TokenManagerListener.getInstance(this).onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TokenManagerListener.getInstance(this).onActivityPaused(this);
    }
}
