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

import com.sun.jna.NativeLong;

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
    private static volatile TokenManagerListener mInstance = null;

    private Context mContext;
    public static final String MAIN_ACTIVITY_IDENTIFIER = TokenManagerListener.class.getName() + "MAIN_ACTIVITY";
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

    public static final NativeLong NO_SLOT = new NativeLong(-1);
    public static final NativeLong NO_CERTIFICATE = new NativeLong(0);
    public static final NativeLong MORE_THAN_ONE_CERTIFICATE = new NativeLong(-2);

    private NativeLong mSlotId = NO_SLOT;
    private Token mToken = null;
    private NativeLong mCertificate = NO_CERTIFICATE;

    private Token mWaitToken = null;
    private NativeLong mWaitCertificate = NO_CERTIFICATE;

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

        NativeLong slotId = (NativeLong) intent.getSerializableExtra(TokenManager.EXTRA_SLOT_ID);
        if (slotId == null) return;

        Token token = TokenManager.getInstance().tokenForSlot(slotId);
        RtPkcs11 rtPkcs11 = RtPkcs11Library.getInstance();

        try {
            token.openSession(rtPkcs11);
            token.initCertificatesList(rtPkcs11);

            processConnectedToken(slotId, token);
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
        NativeLong slotId = (NativeLong) intent.getSerializableExtra(TokenManager.EXTRA_SLOT_ID);
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

    @SuppressWarnings("EmptyMethod")
    protected void onInternalError() {
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
                NativeLong foundCertificate = null;
                for (NativeLong certificate : certificates) {
                    if (token.getCertificate(certificate).getSubject().equals(mWaitToken.getCertificate(mWaitCertificate).getSubject())) {
                        foundCertificate = certificate;
                        break;
                    }
                }
                if (foundCertificate != null) {
                    mSlotId = slotId;
                    mToken = token;
                    mCertificate = foundCertificate;
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
                if (certificates.size() > 1) mCertificate = MORE_THAN_ONE_CERTIFICATE;
                else mCertificate = certificates.iterator().next();
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

    private void notifyAboutTokenError(String errorMsg) {
        errorMsg = mMainActivity.getString(R.string.failed_to_add_token) + " : " + errorMsg;
        Toast.makeText(mMainActivity, errorMsg, Toast.LENGTH_SHORT).show();
    }
}
