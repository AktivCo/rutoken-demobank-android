/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui;

abstract class ExternallyDismissibleActivity extends GuaranteedChildStartActivity {
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
