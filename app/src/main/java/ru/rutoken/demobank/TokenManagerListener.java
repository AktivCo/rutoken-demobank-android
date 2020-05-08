/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.pkcs11caller.TokenManager;

public class TokenManagerListener implements TokenManager.Listener {
    public static final String NO_TOKEN = "";
    public static final String NO_FINGERPRINT = "";
    private static TokenManagerListener INSTANCE;

    private final Context mContext;
    private final List<ManagedActivity> mActivities = Collections.synchronizedList(new LinkedList<>());
    private MainActivity mMainActivity = null;
    // Activities state change logic flags
    private boolean mWaitingForMainActivity = false;
    private boolean mPaymentsCreated = false;
    private boolean mDoWait = false;
    private int mTokenAddingCounter = 0;
    private String mTokenSerial = NO_TOKEN;
    private Token mToken = null;
    private String mCertificateFingerprint = NO_FINGERPRINT;

    private Token mWaitToken = null;
    private String mWaitCertificateFingerprint = NO_FINGERPRINT;

    private TokenManagerListener(Context context) {
        mContext = context.getApplicationContext();
        TokenManager.getInstance().addListener(this);
    }

    public static synchronized TokenManagerListener getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new TokenManagerListener(context);
        return INSTANCE;
    }

    @Override
    public void onEnumerationFinished() {
    }

    @Override
    public void onTokenAdding(long slotId) {
        ++mTokenAddingCounter;
        if (mMainActivity != null)
            mMainActivity.updateScreen();
    }

    @Override
    public void onTokenAddingFailed(long slotId, String error) {
        --mTokenAddingCounter;
        if (mMainActivity != null)
            mMainActivity.updateScreen();

        showToast(mContext.getString(R.string.failed_to_add_token) + " : " + error);
    }

    @Override
    public void onTokenAdded(String tokenSerial) {
        --mTokenAddingCounter;
        if (mMainActivity != null)
            mMainActivity.updateScreen();

        processConnectedToken(TokenManager.getInstance().getTokenBySerial(tokenSerial));
    }

    @Override
    public void onTokenRemoved(String tokenSerial) {
        if (tokenSerial == null) return;

        if (tokenSerial.equals(mTokenSerial)) {
            if (mPaymentsCreated) {
                mPaymentsCreated = false;
                mDoWait = true;
                mWaitCertificateFingerprint = mCertificateFingerprint;
                mWaitToken = mToken;
            }

            resetTokenInfo();

            if (mMainActivity != null)
                mMainActivity.updateScreen();

            for (String serial : TokenManager.getInstance().getTokenSerials()) {
                if (!mTokenSerial.equals(NO_TOKEN))
                    break;
                processConnectedToken(TokenManager.getInstance().getTokenBySerial(serial));
            }

            if (mMainActivity == null) {
                for (ManagedActivity activity : mActivities) {
                    activity.finishExternally();
                }
                mWaitingForMainActivity = true;
            }
        }
    }

    @Override
    public void onInternalError() {
        showToast(mContext.getString(R.string.error));
    }

    private void resetTokenInfo() {
        mTokenSerial = NO_TOKEN;
        mToken = null;
        mCertificateFingerprint = NO_FINGERPRINT;
    }

    private void resetWaitTokenState() {
        mDoWait = false;
        mWaitToken = null;
        mWaitCertificateFingerprint = NO_FINGERPRINT;
    }

    private void processConnectedToken(@Nullable Token token) {
        if (token == null)
            return;
        String tokenSerial = token.getSerialNumber();
        Set<String> certificateFingerprints = token.enumerateCertificates();

        if (mDoWait) { // process wait token once
            do {
                if (certificateFingerprints.isEmpty()) break;

                if (!tokenSerial.equals(mWaitToken.getSerialNumber())) break;

                String certFingerprint = null;
                for (String fp : certificateFingerprints) {
                    if (fp.equals(mWaitCertificateFingerprint)) {
                        certFingerprint = fp;
                        break;
                    }
                }

                if (certFingerprint != null) {
                    mTokenSerial = tokenSerial;
                    mToken = token;
                    mCertificateFingerprint = certFingerprint;
                    if (mMainActivity != null)
                        mMainActivity.startPINActivity();
                    break;
                }
            } while (false);
        }

        if (mDoWait)
            // if the expected device was connected it was processed in the previous block
            resetWaitForToken();

        if (mTokenSerial.equals(NO_TOKEN)) {
            mTokenSerial = tokenSerial;
            mToken = token;
            if (certificateFingerprints.iterator().hasNext()) {
                mCertificateFingerprint = certificateFingerprints.iterator().next();
            } else {
                mCertificateFingerprint = NO_FINGERPRINT;
            }

            if (mMainActivity != null)
                mMainActivity.updateScreen();
        }
    }

    void onActivityResumed(ManagedActivity activity) {
        mActivities.add(activity);
        if (activity.getActivityClassIdentifier().equals(MainActivity.IDENTIFIER)) {
            mMainActivity = (MainActivity) activity;
            mMainActivity.updateScreen();
            mWaitingForMainActivity = false;
        } else {
            if (mWaitingForMainActivity) {
                for (ManagedActivity a : mActivities) {
                    a.finishExternally();
                }
            }
        }
    }

    void onActivityPaused(ManagedActivity activity) {
        mActivities.remove(activity);
        if (activity.getActivityClassIdentifier().equals(MainActivity.IDENTIFIER)) {
            mMainActivity = null;
        }
    }

    boolean shallShowProgressBar() {
        return (mTokenAddingCounter > 0 && !hasToken());
    }

    boolean shallWaitForToken() {
        return mDoWait;
    }

    void resetWaitForToken() {
        resetWaitTokenState();
        if (mMainActivity != null)
            mMainActivity.updateScreen();

        onTokenRemoved(NO_TOKEN);
    }

    String getTokenSerial() {
        return mTokenSerial;
    }

    public Token getToken() {
        return mToken;
    }

    String getCertificateFingerprint() {
        return mCertificateFingerprint;
    }

    Token getWaitToken() {
        return mWaitToken;
    }

    public void setPaymentsCreated() {
        mPaymentsCreated = true;
    }

    public void resetPaymentsCreated() {
        mPaymentsCreated = false;
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private boolean hasToken() {
        return !mTokenSerial.equals(NO_TOKEN);
    }
}
