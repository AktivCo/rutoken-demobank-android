package ru.rutoken.Pkcs11Caller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sun.jna.ptr.IntByReference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11.CK_SLOT_INFO;
import ru.rutoken.Pkcs11.CK_TOKEN_INFO;
import ru.rutoken.Pkcs11.CK_TOKEN_INFO_EXTENDED;
import ru.rutoken.Pkcs11.Pkcs11Constants;

/**
 * Created by mironenko on 07.08.2014.
 */
public class TokenManager {
    private static TokenManager instance = null;
    private EventHandler mEventHandler;
    private Context mContext;
    private Map<String, Integer> mSerialSlotMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    private Map<Integer, Token> mTokens = Collections.synchronizedMap(new HashMap<Integer, Token>());
    private Map<String, Method> mEventReceivers;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            receive(intent);
        }
    };
//    private boolean mFollowSlot = true;
//    private boolean mWaitingForToken = false;
//    private int mSlotId = -1;
//
//    private Thread mEasyHandler;

    private void receive(Intent intent) {
        try {
            mEventReceivers.get(intent.getAction()).invoke(this, intent);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.e(getClass().getName(), "InvocationTargetException");
        }
    }

    public static final String ENUMERATION_FINISHED = TokenManager.class.getName()+".ENUMERATION_FINISHED";
    public static final String TOKEN_WILL_BE_ADDED = EventHandler.class.getName()+".TOKEN_WILL_BE_ADDED";
    public static final String TOKEN_ADDING_FAILED = EventHandler.class.getName()+".TOKEN_ADDING_FAILED";
    public static final String TOKEN_WAS_ADDED = EventHandler.class.getName()+".TOKEN_WAS_ADDED";
    public static final String TOKEN_WAS_REMOVED = EventHandler.class.getName()+".TOKEN_WAS_REMOVED";
    public static final String INTERNAL_ERROR = EventHandler.class.getName()+".INTERNAL_ERROR";


    private TokenManager() {
        Map<String,Method> temp = new HashMap<String, Method>();
        try {
            temp.put(EventHandler.SLOT_HAS_EVENT, TokenManager.class.getDeclaredMethod("slotHasEvent", new Class[] {Intent.class}));
            temp.put(EventHandler.ENUMERATION_FINISHED, TokenManager.class.getDeclaredMethod("enumerationFinished", new Class[] {Intent.class}));
            temp.put(EventHandler.SLOT_WILL_HAVE_TOKEN, TokenManager.class.getDeclaredMethod("slotWillHaveToken", new Class[] {Intent.class}));
            //temp.put(EventHandler.SLOT_EVENT_FAILED, TokenManager.class.getDeclaredMethod("slotEventFailed", new Class[] {Intent.class}));
            //temp.put(EventHandler.EVENT_HANDLER_FAILED, TokenManager.class.getDeclaredMethod("eventHandlerFailed", new Class[] {Intent.class}));
        } catch (NoSuchMethodException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        mEventReceivers = Collections.unmodifiableMap(temp);
    }

    public synchronized void processEvent(EventType event, int slotId, Token token) {
        if(event == EventType.EVENT_HANDLER_FAILED) {
            eventHandlerFailed();
            return;
        }
        if(event == EventType.ENUMERATION_FINISHED) {
            enumerationFinished();
            return;
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

        IntentFilter intentFilter = new IntentFilter();
        for (String action: mEventReceivers.keySet()) {
            intentFilter.addAction(action);
        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);

        mEventHandler = new EventHandler(mContext);
        mEventHandler.start();
//
//        mEasyHandler = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    int rv = RtPkcs11Library.getInstance().C_Initialize(null);
//                    if (Pkcs11Constants.CKR_OK != rv) {
//                        throw Pkcs11Exception.exceptionWithCode(rv);
//                    }
//                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(CRYPTOKI_INITIALIZED));
//                } catch(Exception e){
//                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(EVENT_HANDLER_FAILED));
//                }
//
//                while (true) {
//                    try {
//                        IntByReference slotCount = new IntByReference(0);
//                        int rv = RtPkcs11Library.getInstance().C_GetSlotList(false, null, slotCount);
//                        if (Pkcs11Constants.CKR_OK != rv) {
//                            throw Pkcs11Exception.exceptionWithCode(rv);
//                        }
//                        int slotIds[] = new int[slotCount.getValue()];
//                        rv = RtPkcs11Library.getInstance().C_GetSlotList(true, slotIds, slotCount);
//                        if (Pkcs11Constants.CKR_OK != rv) {
//                            throw Pkcs11Exception.exceptionWithCode(rv);
//                        }
//                        for (int i = 0; i != slotCount.getValue(); ++i) {
//                            if(mFollowSlot && (mSlotId == -1)) slotEventHappened(i);
//                            else break;
//                        }
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ENUMERATION_FINISHED));
//                    } catch(Exception e) {
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(EVENT_HANDLER_FAILED));
//                    }
//
//                    while (true) {
//                        IntByReference id = new IntByReference();
//                        int rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(0, id, null);
//                        if (Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED == rv) {
//                            return;
//                        }
//                        try {
//                            if (Pkcs11Constants.CKR_OK != rv) {
//                                throw Pkcs11Exception.exceptionWithCode(rv);
//                            }
//                            int slotId = id.getValue();
//                            if (mFollowSlot && (mSlotId == -1)) slotEventHappened(id.getValue()); // token inserted -- no reenum
//                            else if (mFollowSlot && (mSlotId == slotId)) {
//                                slotEventHappened(id.getValue());
//                                if (mSlotId == -1) break; // token removed -- need reenum
//                            }
//                            else if (!mFollowSlot && (mSlotId == -1)) slotEventHappened(id.getValue());
//                            else if (!mFollowSlot) slotEventHappened(id.getValue());
//                        } catch (Exception e) {
//                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(EVENT_HANDLER_FAILED));
//                            continue;
//                        }
//                    }
//                }
//            }
//        };
    }

//    protected void slotEventHappened(int id) throws Pkcs11Exception{
//        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
//        int rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);
//        if (Pkcs11Constants.CKR_OK != rv) {
//            throw Pkcs11Exception.exceptionWithCode(rv);
//        }
//        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
//        CK_TOKEN_INFO_EXTENDED extendedInfo = new CK_TOKEN_INFO_EXTENDED();
//        extendedInfo.ulSizeofThisStructure = extendedInfo.size();
//
//        Intent dict = new Intent();
//        dict.putExtra("slotId", id).putExtra("slotInfo", new SlotInfo(slotInfo));
//
//        if ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.flags) != 0x00) {
//            LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_WILL_HAVE_TOKEN));
//            try {
//                rv = RtPkcs11Library.getInstance().C_GetTokenInfo(id, tokenInfo);
//                if (Pkcs11Constants.CKR_OK != rv) {
//                    throw Pkcs11Exception.exceptionWithCode(rv);
//                }
//                rv = RtPkcs11Library.getInstance().C_EX_GetTokenInfoExtended(id, extendedInfo);
//                if (Pkcs11Constants.CKR_OK != rv) {
//                    throw Pkcs11Exception.exceptionWithCode(rv);
//                }
//            } catch (Exception e) {
//                LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_EVENT_FAILED));
//                return;
//            }
//        }
//        dict.putExtra("tokenInfo", new TokenInfo(tokenInfo));
//        dict.putExtra("extendedTokenInfo", new TokenInfoEx(extendedInfo));
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_HAS_EVENT));
//    }

    public synchronized void destroy() {
        if(null == mContext)
            return;
        RtPkcs11Library.getInstance().C_Finalize(null);
        try {
            mEventHandler.join();
        } catch (InterruptedException e) { }
        for (String serial: mSerialSlotMap.keySet()) {
            Intent dict = (new Intent(TOKEN_WAS_REMOVED)).putExtra("serialNumber", serial);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict);
        }
        mSerialSlotMap.clear();
        mTokens.clear();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        mContext = null;
    }

    private void enumerationFinished() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ENUMERATION_FINISHED));
    };

    private synchronized void slotHasEvent(Intent intent) {
        int slotId = intent.getIntExtra("slotId", -1);
        SlotInfo slotInfo = intent.getParcelableExtra("slotInfo");
        TokenInfo tokenInfo = intent.getParcelableExtra("tokenInfo");
        TokenInfoEx tokenInfoEx = intent.getParcelableExtra("extendedTokenInfo");

        Token token = mTokens.get(slotId);

        if (null == token && ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.mSlotInfo.flags) != 0x00)) {
            token = new Token(slotId, slotInfo, tokenInfo, tokenInfoEx);
            mTokens.put(slotId,token);
            mSerialSlotMap.put(token.serialNumber(), slotId);
            Intent dict = (new Intent(TOKEN_WAS_ADDED)).putExtra("serialNumber", token.serialNumber());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict);
        } else if (null != token && ((Pkcs11Constants.CKF_TOKEN_PRESENT & ~(slotInfo.mSlotInfo.flags)) != 0x00)) {
            mSerialSlotMap.remove(token.serialNumber());
            mTokens.remove(slotId);
            Intent dict = (new Intent(TOKEN_WAS_REMOVED)).putExtra("serialNumber", token.serialNumber());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict);
        }
    };
    private void slotWillHaveToken(Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(TOKEN_WILL_BE_ADDED));
    };
    private void slotEventFailed(Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(TOKEN_ADDING_FAILED));
    };
    private void eventHandlerFailed() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(INTERNAL_ERROR));
    };

    public boolean hasToken() {
        return 0 != mTokens.size();
    };
    public synchronized String[] serialNumbers() {
        ArrayList<String> serials = new ArrayList<String>();
        for (Token t: mTokens.values()) {
            serials.add(t.serialNumber());
        }
        String[] serialsArray = new String[serials.size()];
        return serials.toArray(serialsArray);
    };
    public Token tokenForSerialNumber(String serialNumber) {
        Integer slotId = mSerialSlotMap.get(serialNumber);
        if (null == slotId) {
            return null;
        }
        return mTokens.get(slotId);
    }
}
