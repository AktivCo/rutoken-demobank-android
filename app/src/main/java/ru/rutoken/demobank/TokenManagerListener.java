/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.rutoken.pkcs11caller.RtPkcs11Library;
import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.pkcs11caller.TokenManager;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11jna.RtPkcs11;

public class TokenManagerListener {
    public static final String MAIN_ACTIVITY_IDENTIFIER = TokenManagerListener.class.getName() + "MAIN_ACTIVITY";

    public static final String NO_TOKEN = "";
    public static final String NO_FINGERPRINT = "";

    private static volatile TokenManagerListener mInstance = null;

    private Context mContext;
    private MainActivity mMainActivity = null;
    private final List<ManagedActivity> mActivities = Collections.synchronizedList(new LinkedList<>());

    private final BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Objects.equals(action, TokenManager.ENUMERATION_FINISHED)) {
                onEnumerationFinished();
            } else if (Objects.equals(action, TokenManager.TOKEN_WILL_BE_ADDED)) {
                onTokenWillBeAdded(intent);
            } else if (Objects.equals(action, TokenManager.TOKEN_ADDING_FAILED)) {
                onTokenAddingFailed(intent);
            } else if (Objects.equals(action, TokenManager.TOKEN_WAS_ADDED)) {
                onTokenAdded(intent);
            } else if (Objects.equals(action, TokenManager.TOKEN_WAS_REMOVED)) {
                onTokenRemoved(intent);
            } else if (Objects.equals(action, TokenManager.INTERNAL_ERROR)) {
                onInternalError();
            }
        }
    };

    // Activities state change logic flags
    private boolean mWaitingForMainActivity = false;
    private boolean mPaymentsCreated = false;
    private boolean mDoWait = false;

    private int mTwbaCounter = 0;

    private String mTokenSerial = NO_TOKEN;
    private Token mToken = null;
    private String mCertificateFingerprint = NO_FINGERPRINT;

    private Token mWaitToken = null;
    private String mWaitCertificateFingerprint = NO_FINGERPRINT;

    public static synchronized TokenManagerListener getInstance() {
        TokenManagerListener localInstance = mInstance;
        if (localInstance == null) {
            synchronized (TokenManager.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new TokenManagerListener();
                }
            }
        }
        return localInstance;
    }

    public synchronized void init(Context context) {
        if (mContext != null)
            return;

        mContext = context;
        TokenManager.getInstance().init(mContext);

        IntentFilter tmFilter = new IntentFilter();
        tmFilter.addAction(TokenManager.ENUMERATION_FINISHED);
        tmFilter.addAction(TokenManager.TOKEN_WILL_BE_ADDED);
        tmFilter.addAction(TokenManager.TOKEN_ADDING_FAILED);
        tmFilter.addAction(TokenManager.TOKEN_WAS_ADDED);
        tmFilter.addAction(TokenManager.TOKEN_WAS_REMOVED);
        tmFilter.addAction(TokenManager.INTERNAL_ERROR);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mTokenReceiver, tmFilter);
    }

    public synchronized void destroy() {
        if (mContext == null)
            return;

        TokenManager.getInstance().destroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mTokenReceiver);
        mInstance = null;
    }

    @SuppressWarnings("EmptyMethod")
    protected void onEnumerationFinished() {
    }

    protected void onTokenWillBeAdded(Intent intent) {
        ++mTwbaCounter;
        if (mMainActivity != null) mMainActivity.updateScreen();
    }

    protected void onTokenAddingFailed(Intent intent) {
        --mTwbaCounter;
        if (mMainActivity != null) {
            mMainActivity.updateScreen();
            notifyAboutTokenError(intent.getStringExtra(TokenManager.EXTRA_TOKEN_ERROR));
        }
    }

    protected void onTokenAdded(Intent intent) {
        --mTwbaCounter;
        if (mMainActivity != null) mMainActivity.updateScreen();

        String tokenSerial = intent.getStringExtra(TokenManager.EXTRA_TOKEN_SERIAL);
        if (tokenSerial == null) return;

        Token token = TokenManager.getInstance().tokenForId(tokenSerial);
        RtPkcs11 rtPkcs11 = RtPkcs11Library.getInstance();

        try {
            token.openSession(rtPkcs11);
            token.readCertificates(rtPkcs11);

            processConnectedToken(token);
            if (mMainActivity != null) mMainActivity.updateScreen();
        } catch (Pkcs11CallerException e) {
            e.printStackTrace();
            token.closeSession(rtPkcs11);

            if (mMainActivity != null) {
                if (mToken == null && token.smInitializedStatus() == Token.SmInitializedStatus.NEED_INITIALIZE)
                    mMainActivity.updateScreen(token);
                else
                    notifyAboutTokenError(e.getMessage());
            }
        }
    }

    protected void onTokenRemoved(Intent intent) {
        String tokenSerial = intent.getStringExtra(TokenManager.EXTRA_TOKEN_SERIAL);
        onTokenRemoved(tokenSerial);
    }

    protected void onTokenRemoved(String tokenSerial) {
        if (tokenSerial == null) return;

        if (tokenSerial.equals(mTokenSerial)) {
            if (mPaymentsCreated) {
                mPaymentsCreated = false;
                mDoWait = true;
                mWaitCertificateFingerprint = mCertificateFingerprint;
                mWaitToken = mToken;
            }

            resetTokenInfo();

            if (mMainActivity != null) mMainActivity.updateScreen();

            for (String id : TokenManager.getInstance().tokenSerials()) {
                if (!mTokenSerial.equals(NO_TOKEN))
                    break;
                processConnectedToken(TokenManager.getInstance().tokenForId(id));
            }

            if (mMainActivity == null) {
                for (ManagedActivity activity : mActivities) {
                    activity.finishExternally();
                }
                mWaitingForMainActivity = true;
            }
        }
    }

    @SuppressWarnings("EmptyMethod")
    protected void onInternalError() {
    }

    protected void resetTokenInfo() {
        mTokenSerial = NO_TOKEN;
        mToken = null;
        mCertificateFingerprint = NO_FINGERPRINT;
    }

    protected void resetWaitTokenState() {
        mDoWait = false;
        mWaitToken = null;
        mWaitCertificateFingerprint = NO_FINGERPRINT;
    }

    protected void processConnectedToken(Token token) {
        if (token == null) return;
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
                    if (mMainActivity != null) mMainActivity.startPINActivity();
                    break;
                }
            } while (false);
        }
        // Process new token, if need
        if (mTokenSerial.equals(NO_TOKEN)) {
            mTokenSerial = tokenSerial;
            mToken = token;
            if (certificateFingerprints.iterator().hasNext()) {
                mCertificateFingerprint = certificateFingerprints.iterator().next();
            } else {
                mCertificateFingerprint = NO_FINGERPRINT;
            }

            if (mMainActivity != null) mMainActivity.updateScreen();
        }
    }

    public void onActivityResumed(ManagedActivity activity) {
        mActivities.add(activity);
        if (activity.getActivityClassIdentifier().equals(MAIN_ACTIVITY_IDENTIFIER)) {
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

    public void onActivityPaused(ManagedActivity activity) {
        mActivities.remove(activity);
        if (activity.getActivityClassIdentifier().equals(MAIN_ACTIVITY_IDENTIFIER)) {
            mMainActivity = null;
        }
    }

    public boolean shallShowProgressBar() {
        return (mTwbaCounter > 0);
    }

    public boolean shallWaitForToken() {
        return mDoWait;
    }

    public void resetWaitForToken() {
        resetWaitTokenState();
        if (mMainActivity != null) mMainActivity.updateScreen();
        onTokenRemoved(NO_TOKEN);
    }

    public String getTokenSerial() {
        return mTokenSerial;
    }

    public Token getToken() {
        return mToken;
    }

    public String getCertificateFingerprint() {
        return mCertificateFingerprint;
    }

    public Token getWaitToken() {
        return mWaitToken;
    }

    public void setPaymentsCreated() {
        mPaymentsCreated = true;
    }

    public void resetPaymentsCreated() {
        mPaymentsCreated = false;
    }

    private void notifyAboutTokenError(String errorMsg) {
        errorMsg = mMainActivity.getString(R.string.failed_to_add_token) + " : " + errorMsg;
        Toast.makeText(mMainActivity, errorMsg, Toast.LENGTH_SHORT).show();
    }
}
