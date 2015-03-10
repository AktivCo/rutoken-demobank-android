/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.sun.jna.NativeLong;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.TokenManager;

public class TokenManagerListener {
    private static TokenManagerListener mInstance = null;

    private Context mContext;
    public static final String MAIN_ACTIVITY_IDENTIFIER = TokenManagerListener.class.getName() + "MAIN_ACTIVITY";
    private MainActivity mMainActivity = null;
    private List<ManagedActivity> mActivities = Collections.synchronizedList(new LinkedList<ManagedActivity>());

    private final BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(TokenManager.ENUMERATION_FINISHED)) {
                onEnumerationFinished();
            } else if (action.equals(TokenManager.TOKEN_WILL_BE_ADDED)) {
                onTokenWillBeAdded(intent);
            } else if (action.equals(TokenManager.TOKEN_ADDING_FAILED)) {
                onTokenAddingFailed(intent);
            } else if (action.equals(TokenManager.TOKEN_WAS_ADDED)) {
                onTokenAdded(intent);
            } else if (action.equals(TokenManager.TOKEN_WAS_REMOVED)) {
                onTokenRemoved(intent);
            } else if (action.equals(TokenManager.INTERNAL_ERROR)) {
                onInternalError();
            }
        }
    };

    // Activities state change logic flags
    private boolean mWaitingForMainActivity = false;
    private boolean mPaymentsCreated = false;
    private boolean mDoWait = false;

    private int mTwbaCounter = 0;

    public static final NativeLong NO_SLOT = new NativeLong(-1);
    public static final NativeLong NO_CERTIFICATE = new NativeLong(0);

    protected NativeLong mSlotId = NO_SLOT;
    protected Token mToken = null;
    protected NativeLong mCertificate = NO_CERTIFICATE;

    protected Token mWaitToken = null;
    protected NativeLong mWaitCertificate = NO_CERTIFICATE;

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

    protected void onEnumerationFinished() {
        // Do nothing
    }

    protected void onTokenWillBeAdded(Intent intent) {
        ++mTwbaCounter;
        if (mMainActivity != null) mMainActivity.updateScreen();
    }

    protected void onTokenAddingFailed(Intent intent) {
        --mTwbaCounter;
        if (mMainActivity != null) mMainActivity.updateScreen();

    }

    protected void onTokenAdded(Intent intent) {
        --mTwbaCounter;
        if (mMainActivity != null) mMainActivity.updateScreen();

        NativeLong slotId = (NativeLong) intent.getSerializableExtra("slotId");
        if (slotId == null) return;

        Token token = TokenManager.getInstance().tokenForSlot(slotId);
        processConnectedToken(slotId, token);
    }

    protected void onTokenRemoved(Intent intent) {
        NativeLong slotId = (NativeLong) intent.getSerializableExtra("slotId");
        onTokenRemoved(slotId);
    }

    protected void onTokenRemoved(NativeLong slotId) {
        if (slotId == null) return;

        if (slotId.equals(mSlotId)) {
            if (mPaymentsCreated) {
                mPaymentsCreated = false;
                mDoWait = true;
                mWaitCertificate = mCertificate;
                mWaitToken = mToken;
            }

            resetSlotInfo();

            if (mMainActivity != null) mMainActivity.updateScreen();

            for (NativeLong slot : TokenManager.getInstance().slots()) {
                if (!mSlotId.equals(NO_SLOT))
                    break;
                processConnectedToken(slot, TokenManager.getInstance().tokenForSlot(slot));
            }

            if (mMainActivity == null) {
                for (ManagedActivity activity : mActivities) {
                    activity.finishExternally();
                }
                mWaitingForMainActivity = true;
            }
        }
    }

    protected void onInternalError() {
        // Do nothing
    }

    protected void resetSlotInfo() {
        mSlotId = NO_SLOT;
        mToken = null;
        mCertificate = NO_CERTIFICATE;
    }

    protected void resetWaitSlotState() {
        mDoWait = false;
        mWaitToken = null;
        mWaitCertificate = NO_CERTIFICATE;
    }

    protected void processConnectedToken(NativeLong slotId, Token token) {
        if (token == null) return;

        String serial = token.getSerialNumber();
        Set<NativeLong> certificates = token.enumerateCertificates();

        if (mDoWait) { // process wait token once
            do {
                if (certificates == null) break;
                if (!serial.equals(mWaitToken.getSerialNumber())) break;
                NativeLong foundCerificate = null;
                for (NativeLong certificate : certificates) {
                    if (token.getCertificate(certificate).getSubject().equals(mWaitToken.getCertificate(mWaitCertificate).getSubject())) {
                        foundCerificate = certificate;
                        break;
                    }
                }
                if (foundCerificate != null) {
                    mSlotId = slotId;
                    mToken = token;
                    mCertificate = foundCerificate;
                    if (mMainActivity != null) mMainActivity.startPINActivity();
                    break;
                }
            } while (false);
        }
        // Process new token, if need
        if (mSlotId.equals(NO_SLOT)) {
            mSlotId = slotId;
            mToken = token;
            if (certificates != null && certificates.iterator().hasNext()) {
                mCertificate = certificates.iterator().next();
            } else {
                mCertificate = NO_CERTIFICATE;
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
        resetWaitSlotState();
        if (mMainActivity != null) mMainActivity.updateScreen();
        onTokenRemoved(NO_SLOT);
    }

    public NativeLong getSlotId() {
        return mSlotId;
    }

    public Token getToken() {
        return mToken;
    }

    public NativeLong getCertificate() {
        return mCertificate;
    }

    public Token getWaitToken() {
        return mWaitToken;
    }

    public NativeLong getWaitCertificate() {
        return mWaitCertificate;
    }

    public void setPaymentsCreated() {
        mPaymentsCreated = true;
    }

    public void resetPaymentsCreated() {
        mPaymentsCreated = false;
    }

}
