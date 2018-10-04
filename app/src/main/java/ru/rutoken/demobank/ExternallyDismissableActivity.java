/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

abstract class ExternallyDismissableActivity extends GuaranteedChildStartActivity {
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
