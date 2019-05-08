/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.sun.jna.NativeLong;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;

public class TokenManager {
    private class TokenInfoLoader extends Thread {
        private final NativeLong mSlotId;
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        TokenInfoLoader(NativeLong slotId) {
            mSlotId = slotId;
        }

        public void run() {
            EventType event = EventType.TOKEN_INFO_LOADED;
            Token token = null;

            try {
                token = new Token(mSlotId);
            } catch (Pkcs11CallerException e) {
                mTokenError = e;
                e.printStackTrace();
                event = EventType.TOKEN_INFO_FAILED;
            }

            mHandler.post(new EventRunnable(event, mSlotId, token));
        }
    }

    private class TokenManagerException extends Exception {
        TokenManagerException(String what) {
            super(what);
        }
    }

    enum AcceptableState
    {
        R0W0SD,
        R0W1SD,
        R0W0SR,
        R1W0SR,
        R1W0TIL,
        R1W0TIF
    }

    private static volatile TokenManager instance = null;
    private EventHandlerThread mEventHandlerThread;
    private Context mContext;
    private Pkcs11CallerException mTokenError;
    private final Map<NativeLong, Token> mTokens = Collections.synchronizedMap(new HashMap<>());
    private final Map<NativeLong, AcceptableState> mStateMachines = Collections.synchronizedMap(new HashMap<>());
    private final Map<NativeLong, TokenInfoLoader> mTilThreads = Collections.synchronizedMap(new HashMap<>());
    private final Map<AcceptableState, Method> mCurrentStateProcessors;

    public static final String ENUMERATION_FINISHED = TokenManager.class.getName() + ".ENUMERATION_FINISHED";
    public static final String TOKEN_WILL_BE_ADDED = EventHandlerThread.class.getName() + ".TOKEN_WILL_BE_ADDED";
    public static final String TOKEN_ADDING_FAILED = EventHandlerThread.class.getName() + ".TOKEN_ADDING_FAILED";
    public static final String TOKEN_WAS_ADDED = EventHandlerThread.class.getName() + ".TOKEN_WAS_ADDED";
    public static final String TOKEN_WAS_REMOVED = EventHandlerThread.class.getName() + ".TOKEN_WAS_REMOVED";
    public static final String INTERNAL_ERROR = EventHandlerThread.class.getName() + ".INTERNAL_ERROR";

    public static final String EXTRA_SLOT_ID = TokenManager.class.getName() + ".SLOT_ID";
    public static final String EXTRA_TOKEN_ERROR = TokenManager.class.getName() + ".TOKEN_ERROR";

    private TokenManager() {
        Map<AcceptableState, Method> tmp = new HashMap<>();
        try {
            tmp.put(AcceptableState.R0W0SD, TokenManager.class.getDeclaredMethod("processCurrentStateR0W0SD", EventType.class, NativeLong.class, Token.class));
            tmp.put(AcceptableState.R0W1SD, TokenManager.class.getDeclaredMethod("processCurrentStateR0W1SD", EventType.class, NativeLong.class, Token.class));
            tmp.put(AcceptableState.R0W0SR, TokenManager.class.getDeclaredMethod("processCurrentStateR0W0SR", EventType.class, NativeLong.class, Token.class));
            tmp.put(AcceptableState.R1W0SR, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0SR", EventType.class, NativeLong.class, Token.class));
            tmp.put(AcceptableState.R1W0TIL, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0TIL", EventType.class, NativeLong.class, Token.class));
            tmp.put(AcceptableState.R1W0TIF, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0TIF", EventType.class, NativeLong.class, Token.class));
        } catch (NoSuchMethodException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        mCurrentStateProcessors = tmp;
    }

    synchronized void startTilThread(NativeLong slotId) {
        TokenInfoLoader til = new TokenInfoLoader(slotId);
        til.start();
        mTilThreads.put(slotId, til);
    }

    AcceptableState processCurrentStateR0W0SD(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                throw new TokenManagerException("Input not accepted by state");
            case SLOT_REMOVED:
                newState = AcceptableState.R0W0SR;
                sendTAF(slotId);
                break;
            case TOKEN_INFO_LOADED:
            case TOKEN_INFO_FAILED:
                newState = AcceptableState.R0W1SD;
                startTilThread(slotId);
                break;
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    AcceptableState processCurrentStateR0W1SD(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                throw new TokenManagerException("Input not accepted by state");
            case SLOT_REMOVED:
                newState = AcceptableState.R0W0SR;
                sendTAF(slotId);
                break;
            case TOKEN_INFO_LOADED:
                newState = AcceptableState.R1W0TIL;
                mTokens.put(slotId, token);
                sendTA(slotId);
                break;
            case TOKEN_INFO_FAILED:
                newState = AcceptableState.R1W0TIF;
                sendTAF(slotId);
                break;
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    AcceptableState processCurrentStateR0W0SR(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                newState = AcceptableState.R0W0SD;
                sendTWBA(slotId);
                break;
            case SLOT_REMOVED:
                throw new TokenManagerException("Input not accepted by state");
            case TOKEN_INFO_LOADED:
                newState = AcceptableState.R1W0TIL;
                break;
            case TOKEN_INFO_FAILED:
                newState = AcceptableState.R1W0TIF;
                break;
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    AcceptableState processCurrentStateR1W0SR(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                startTilThread(slotId);
                break;
            case SLOT_REMOVED:
            case TOKEN_INFO_LOADED:
            case TOKEN_INFO_FAILED:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    AcceptableState processCurrentStateR1W0TIL(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                startTilThread(slotId);
                break;
            case SLOT_REMOVED:
                newState = AcceptableState.R1W0SR;
                mTokens.remove(slotId);
                sendTR(slotId);
                break;
            case TOKEN_INFO_LOADED:
            case TOKEN_INFO_FAILED:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    AcceptableState processCurrentStateR1W0TIF(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SLOT_ADDED:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                startTilThread(slotId);
                break;
            case SLOT_REMOVED:
                newState = AcceptableState.R1W0SR;
                break;
            case TOKEN_INFO_LOADED:
            case TOKEN_INFO_FAILED:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    public synchronized void processEvent(EventType event, NativeLong slotId, Token token) {
        if (event == EventType.EVENT_HANDLER_FAILED) {
            sendEventHandlerFailed();
            return;
        }

        if (event == EventType.ENUMERATION_FINISHED) {
            sendEnumerationFinished();
            return;
        }

        AcceptableState state = mStateMachines.get(slotId);
        if (state == null) {
            state = AcceptableState.R1W0SR;
            mStateMachines.put(slotId, state);
        }

        try {
            state = (AcceptableState) mCurrentStateProcessors.get(state).invoke(this, event, slotId, token);
            mStateMachines.put(slotId, state);
        } catch (InvocationTargetException e) {
            Log.e(getClass().getName(), "InvocationTargetException");
            if (e.getCause() instanceof TokenManagerException) {
                Log.e(getClass().getName(), e.getCause().getMessage());
            }
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "IllegalAccessException");
        }
    }

    public static synchronized TokenManager getInstance() {
        TokenManager localInstance = instance;
        if (localInstance == null) {
            synchronized (TokenManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new TokenManager();
                }
            }
        }
        return localInstance;
    }

    public synchronized void init(Context context) {
        if (mContext != null)
            return;
        mContext = context;

        mEventHandlerThread = new EventHandlerThread();
        mEventHandlerThread.start();
    }

    public synchronized void destroy() {
        if (mContext == null)
            return;

        mEventHandlerThread.interrupt();
        for (TokenInfoLoader loader : mTilThreads.values()) {
            loader.interrupt();
        }
        RtPkcs11Library.getInstance().C_Finalize(null);

        for (NativeLong slotId : mTokens.keySet()) {
            sendTR(slotId);
        }

        mTokens.clear();

        mContext = null;
        instance = null;
    }

    private void sendTWBA(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WILL_BE_ADDED, slotId);
    }

    private void sendTAF(NativeLong slotId) {
        String intentType = TOKEN_ADDING_FAILED;
        Intent intent = new Intent(intentType);
        intent.putExtra(EXTRA_SLOT_ID, slotId);
        intent.putExtra(EXTRA_TOKEN_ERROR, mTokenError.getMessage());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void sendTA(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WAS_ADDED, slotId);
    }

    private void sendTR(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WAS_REMOVED, slotId);
    }

    private void sendIntentWithSlotId(String intentType, NativeLong slotId) {
        Intent intent = new Intent(intentType);
        intent.putExtra(EXTRA_SLOT_ID, slotId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void sendEnumerationFinished() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ENUMERATION_FINISHED));
    }

    private void sendEventHandlerFailed() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTERNAL_ERROR));
    }

    public synchronized NativeLong[] slots() {
        NativeLong[] slots = new NativeLong[mTokens.keySet().size()];
        return mTokens.keySet().toArray(slots);
    }

    public Token tokenForSlot(NativeLong slotId) {
        return mTokens.get(slotId);
    }
}
