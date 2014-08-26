package ru.rutoken.Pkcs11Caller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sun.jna.NativeLong;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;

/**
 * Created by mironenko on 07.08.2014.
 */
public class TokenManager {
    private class TokenInfoLoader extends Thread {
        NativeLong mSlotId;
        Handler mHandler = new Handler(Looper.getMainLooper());
        TokenInfoLoader(NativeLong slotId) {
            mSlotId = slotId;
        }
        public void run() {
            EventType event = EventType.TIL;
            Token token = null;
            try {
                token = new Token(mSlotId);
            } catch (Pkcs11CallerException e) {
                Log.e(getClass().getName(), e.getMessage());
                event = EventType.TIF;
            }
            mHandler.post(new EventRunnable(event, mSlotId, token));
        }
    }

    class TokenManagerException extends Exception {
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

    private static TokenManager instance = null;
    private EventHandler mEventHandler;
    private Context mContext;
    private Map<NativeLong, Token> mTokens = Collections.synchronizedMap(new HashMap<NativeLong, Token>());
    private Map<NativeLong, AcceptableState> stateMachines = Collections.synchronizedMap(new HashMap<NativeLong, AcceptableState>());
    private Map<AcceptableState, Method> mCurrentStateProcessors;

    public static final String ENUMERATION_FINISHED = TokenManager.class.getName()+".ENUMERATION_FINISHED";
    public static final String TOKEN_WILL_BE_ADDED = EventHandler.class.getName()+".TOKEN_WILL_BE_ADDED";
    public static final String TOKEN_ADDING_FAILED = EventHandler.class.getName()+".TOKEN_ADDING_FAILED";
    public static final String TOKEN_WAS_ADDED = EventHandler.class.getName()+".TOKEN_WAS_ADDED";
    public static final String TOKEN_WAS_REMOVED = EventHandler.class.getName()+".TOKEN_WAS_REMOVED";
    public static final String INTERNAL_ERROR = EventHandler.class.getName()+".INTERNAL_ERROR";


    private TokenManager() {
        Map<AcceptableState, Method> tmp = new HashMap<AcceptableState, Method>();
        try {
            tmp.put(AcceptableState.R0W0SD, TokenManager.class.getDeclaredMethod("processCurrentStateR0W0SD", new Class[] {EventType.class, NativeLong.class, Token.class}));
            tmp.put(AcceptableState.R0W1SD, TokenManager.class.getDeclaredMethod("processCurrentStateR0W1SD", new Class[] {EventType.class, NativeLong.class, Token.class}));
            tmp.put(AcceptableState.R0W0SR, TokenManager.class.getDeclaredMethod("processCurrentStateR0W0SR", new Class[] {EventType.class, NativeLong.class, Token.class}));
            tmp.put(AcceptableState.R1W0SR, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0SR", new Class[] {EventType.class, NativeLong.class, Token.class}));
            tmp.put(AcceptableState.R1W0TIL, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0TIL", new Class[] {EventType.class, NativeLong.class, Token.class}));
            tmp.put(AcceptableState.R1W0TIF, TokenManager.class.getDeclaredMethod("processCurrentStateR1W0TIF", new Class[] {EventType.class, NativeLong.class, Token.class}));
        } catch (NoSuchMethodException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        mCurrentStateProcessors = tmp;
    }

    AcceptableState processCurrentStateR0W0SD(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SD:
                throw new TokenManagerException("Input not accepted by state");
            case SR:
                newState = AcceptableState.R0W0SR;
                sendTAF(slotId);
                break;
            case TIL:
            case TIF:
                newState = AcceptableState.R0W1SD;
                (new TokenInfoLoader(slotId)).start();
                break;
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }
    AcceptableState processCurrentStateR0W1SD(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SD:
                throw new TokenManagerException("Input not accepted by state");
            case SR:
                newState = AcceptableState.R0W0SR;
                sendTAF(slotId);
                break;
            case TIL:
                newState = AcceptableState.R1W0TIL;
                mTokens.put(slotId, token);
                sendTA(slotId);
                break;
            case TIF:
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
            case SD:
                newState = AcceptableState.R0W0SD;
                sendTWBA(slotId);
                break;
            case SR:
                throw new TokenManagerException("Input not accepted by state");
            case TIL:
                newState = AcceptableState.R1W0TIL;
                break;
            case TIF:
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
            case SD:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                (new TokenInfoLoader(slotId)).start();
                break;
            case SR:
            case TIL:
            case TIF:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }
    AcceptableState processCurrentStateR1W0TIL(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SD:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                (new TokenInfoLoader(slotId)).start();
                break;
            case SR:
                newState = AcceptableState.R1W0SR;
                mTokens.remove(slotId);
                sendTR(slotId);
                break;
            case TIL:
            case TIF:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }
    AcceptableState processCurrentStateR1W0TIF(EventType event, NativeLong slotId, Token token) throws TokenManagerException {
        AcceptableState newState;
        switch (event) {
            case SD:
                newState = AcceptableState.R0W1SD;
                sendTWBA(slotId);
                (new TokenInfoLoader(slotId)).start();
                break;
            case SR:
                newState = AcceptableState.R1W0SR;
                break;
            case TIL:
            case TIF:
                throw new TokenManagerException("Input not accepted by state");
            default:
                throw new TokenManagerException("Unexpected unfiltered incoming event");
        }
        return newState;
    }

    public synchronized void processEvent(EventType event, NativeLong slotId, Token token) {
        if(event == EventType.EVENT_HANDLER_FAILED) {
            sendEventHandlerFailed();
            return;
        }
        if(event == EventType.ENUMERATION_FINISHED) {
            sendEnumerationFinished();
            return;
        }

        AcceptableState state = stateMachines.get(slotId);
        if(state == null) {
            state = AcceptableState.R1W0SR;
            stateMachines.put(slotId, state);
        }

        try {
            state = (AcceptableState) mCurrentStateProcessors.get(state).invoke(this, event, slotId, token);
            stateMachines.put(slotId, state);
        } catch (InvocationTargetException e) {
            Log.e(getClass().getName(), "InvocationTargetException");
            if (e.getCause() instanceof TokenManagerException) {
                Log.e(getClass().getName(), e.getMessage());
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
        if(null != mContext)
            return;
        mContext = context;

        mEventHandler = new EventHandler(mContext);
        mEventHandler.start();
    }

    public synchronized void destroy() {
        if(null == mContext)
            return;
        synchronized (RtPkcs11Library.getInstance()) {
            RtPkcs11Library.getInstance().C_Finalize(null);
        }
        try {
            mEventHandler.join();
        } catch (InterruptedException e) {
            Log.e(getClass().getName(), "Interrupted exception");
        }

        for (NativeLong slotId: mTokens.keySet()) {
            sendTR(slotId);
        }
        mTokens.clear();

        mContext = null;
    }

    private void sendTWBA(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WILL_BE_ADDED, slotId);
    };
    private void sendTAF(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_ADDING_FAILED, slotId);
    };
    private void sendTA(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WAS_ADDED, slotId);
    };
    private void sendTR(NativeLong slotId) {
        sendIntentWithSlotId(TOKEN_WAS_REMOVED, slotId);
    };
    private void sendIntentWithSlotId(String intentType, NativeLong slotId) {
        Intent intent = new Intent(intentType);
        intent.putExtra("slotId", slotId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
    private void sendEnumerationFinished() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ENUMERATION_FINISHED));
    };
    private void sendEventHandlerFailed() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTERNAL_ERROR));
    };
    public synchronized NativeLong[] slots() {
        NativeLong[] slots = new NativeLong[mTokens.keySet().size()];
        return mTokens.keySet().toArray(slots);
    };
    public Token tokenForSlot(NativeLong slotId) {
        return mTokens.get(slotId);
    }
}
