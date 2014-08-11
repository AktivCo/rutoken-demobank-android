package ru.rutoken.Pkcs11Caller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11.Pkcs11Constants;

/**
 * Created by mironenko on 07.08.2014.
 */
public class Pkcs11 {
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

    private void receive(Intent intent) {
        try {
            mEventReceivers.get(intent.getAction()).invoke(this, intent);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "IllegalAccessException");
        } catch (InvocationTargetException e) {
            Log.e(getClass().getName(), "InvocationTargetException");
        }
    }

    public static final String ENUMERATION_FINISHED = Pkcs11.class.getName()+".ENUMERATION_FINISHED";
    public static final String TOKEN_WILL_BE_ADDED = EventHandler.class.getName()+".TOKEN_WILL_BE_ADDED";
    public static final String TOKEN_ADDING_FAILED = EventHandler.class.getName()+".TOKEN_ADDING_FAILED";
    public static final String TOKEN_WAS_ADDED = EventHandler.class.getName()+".TOKEN_WAS_ADDED";
    public static final String TOKEN_WAS_REMOVED = EventHandler.class.getName()+".TOKEN_WAS_REMOVED";
    public static final String INTERNAL_ERROR = EventHandler.class.getName()+".INTERNAL_ERROR";


    public Pkcs11(Context context) {
        mContext = context;

        Map<String,Method> temp = new HashMap<String, Method>();
        try {
            temp.put(EventHandler.SLOT_HAS_EVENT, Pkcs11.class.getDeclaredMethod("slotHasEvent", new Class[] {Intent.class}));
            temp.put(EventHandler.ENUMERATION_FINISHED, Pkcs11.class.getDeclaredMethod("enumerationFinished", new Class[] {Intent.class}));
            temp.put(EventHandler.SLOT_WILL_HAVE_TOKEN, Pkcs11.class.getDeclaredMethod("slotWillHaveToken", new Class[] {Intent.class}));
            temp.put(EventHandler.SLOT_EVENT_FAILED, Pkcs11.class.getDeclaredMethod("slotEventFailed", new Class[] {Intent.class}));
            temp.put(EventHandler.EVENT_HANDLER_FAILED, Pkcs11.class.getDeclaredMethod("eventHandlerFailed", new Class[] {Intent.class}));
        } catch (NoSuchMethodException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
        mEventReceivers = Collections.unmodifiableMap(temp);

        IntentFilter intentFilter = new IntentFilter();
        for (String action: mEventReceivers.keySet()) {
            intentFilter.addAction(action);
        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, intentFilter);

        mEventHandler = new EventHandler(mContext);
        mEventHandler.start();

    }
    public void destroy() {
        RtPkcs11Library.getInstance().C_Finalize(null);
    }
    private void enumerationFinished(Intent intent) {
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
    private void eventHandlerFailed(Intent intent) {
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
        return (String[])serials.toArray();
    };
    public Token tokenForSerialNumber(String serialNumber) {
        Integer slotId = mSerialSlotMap.get(serialNumber);
        if (null == slotId) {
            return null;
        }
        return mTokens.get(slotId);
    }
}
