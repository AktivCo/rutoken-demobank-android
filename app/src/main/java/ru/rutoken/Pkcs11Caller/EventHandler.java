package ru.rutoken.Pkcs11Caller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.List;

import ru.rutoken.Pkcs11.CK_SLOT_INFO;
import ru.rutoken.Pkcs11.CK_TOKEN_INFO;
import ru.rutoken.Pkcs11.CK_TOKEN_INFO_EXTENDED;
import ru.rutoken.Pkcs11.Pkcs11Constants;

/**
 * Created by mironenko on 07.08.2014.
 */
public class EventHandler extends Thread {
    Context mContext;
    public EventHandler(Context context) {
        mContext = context;
    }
    @Override
    public void run() {
        try {
            int rv = RtPkcs11Library.getInstance().C_Initialize(null);
            if (Pkcs11Constants.CKR_OK != rv) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(CRYPTOKI_INITIALIZED));

            IntByReference slotCount = new IntByReference(0);
            rv = RtPkcs11Library.getInstance().C_GetSlotList(false, null, slotCount);
            if (Pkcs11Constants.CKR_OK != rv) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            int slotIds[] = new int[slotCount.getValue()];
            rv = RtPkcs11Library.getInstance().C_GetSlotList(true, slotIds, slotCount);
            if (Pkcs11Constants.CKR_OK != rv) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            for (int i = 0; i != slotCount.getValue(); ++i) {
                slotEventHappened(i);
            }
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ENUMERATION_FINISHED));
        } catch (Exception e) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(EVENT_HANDLER_FAILED));
        }

        while(true) {
            IntByReference id = new IntByReference();
            int rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(0, id, null);
            if (Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED == rv) {
                return;
            }
            try {
                if (Pkcs11Constants.CKR_OK != rv) {
                    throw Pkcs11Exception.exceptionWithCode(rv);
                }
                slotEventHappened(id.getValue());
            } catch (Exception e) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(EVENT_HANDLER_FAILED));
                return;
            }
        }
    }

    public static final String CRYPTOKI_INITIALIZED = EventHandler.class.getName()+".CRYPTOKI_INITIALIZED";
    public static final String EVENT_HANDLER_FAILED = EventHandler.class.getName()+".EVENT_HANDLER_FAILED";
    public static final String SLOT_WILL_HAVE_TOKEN = EventHandler.class.getName()+".SLOT_WILL_HAVE_TOKEN";
    public static final String SLOT_EVENT_FAILED = EventHandler.class.getName()+".SLOT_EVENT_FAILED";
    public static final String SLOT_HAS_EVENT = EventHandler.class.getName()+".SLOT_HAS_EVENT";
    public static final String ENUMERATION_FINISHED = EventHandler.class.getName()+".ENUMERATION_FINISHED";

    protected void slotEventHappened(int id) throws Pkcs11Exception{
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        int rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
        CK_TOKEN_INFO_EXTENDED extendedInfo = new CK_TOKEN_INFO_EXTENDED();
        extendedInfo.ulSizeofThisStructure = extendedInfo.size();

        Intent dict = new Intent();
        dict.putExtra("slotId", id).putExtra("slotInfo", new SlotInfo(slotInfo));

        if ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.flags) != 0x00) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_WILL_HAVE_TOKEN));
            try {
                rv = RtPkcs11Library.getInstance().C_GetTokenInfo(id, tokenInfo);
                if (Pkcs11Constants.CKR_OK != rv) {
                    throw Pkcs11Exception.exceptionWithCode(rv);
                }
                rv = RtPkcs11Library.getInstance().C_EX_GetTokenInfoExtended(id, extendedInfo);
                if (Pkcs11Constants.CKR_OK != rv) {
                    throw Pkcs11Exception.exceptionWithCode(rv);
                }
            } catch (Exception e) {
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_EVENT_FAILED));
                return;
            }
        }
        dict.putExtra("tokenInfo", new TokenInfo(tokenInfo));
        dict.putExtra("extendedTokenInfo", new TokenInfoEx(extendedInfo));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(dict.setAction(SLOT_HAS_EVENT));
    }
}
