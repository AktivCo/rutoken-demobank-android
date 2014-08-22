package ru.rutoken.Pkcs11Caller;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.sun.jna.ptr.IntByReference;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11.CK_SLOT_INFO;
import ru.rutoken.Pkcs11.Pkcs11Constants;

/**
 * Created by mironenko on 07.08.2014.
 */
public class EventHandler extends Thread {
    Context mContext;
    public EventHandler(Context context) {
        mContext = context;
    }
    Map<Integer, EventType> lastSlotEvent = new HashMap<Integer, EventType>();
    Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void run() {
        try {
            int rv = RtPkcs11Library.getInstance().C_Initialize(null);
            if (Pkcs11Constants.CKR_OK != rv) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }

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
            mHandler.post(new EventRunnable(EventType.ENUMERATION_FINISHED,-1));
        } catch (Exception e) {
            mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED,-1));
        }

        while(true) {
            IntByReference id = new IntByReference();
            int rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(0, id, null);
            if (Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED == rv) {
                Log.d(getClass().getName(), "Exit EH");
                return;
            }
            try {
                if (Pkcs11Constants.CKR_OK != rv) {
                    throw Pkcs11Exception.exceptionWithCode(rv);
                }
                slotEventHappened(id.getValue());
            } catch (Exception e) {
                mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED, -1));
                // TODO ??? return ???
            }
        }
    }

    static EventType oppositeEvent(EventType event) {
        if(event == EventType.SD) return EventType.SR;
        return EventType.SD;
    }

    protected void slotEventHappened(int id) throws Pkcs11Exception{
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        int rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }

        EventType event;
        if ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.flags) != 0x00) {
            event = EventType.SD;
        } else {
            event = EventType.SR;
        }

        if (lastSlotEvent.get(id) == event) {
            mHandler.post(new EventRunnable(oppositeEvent(event),id));
            lastSlotEvent.put(id,oppositeEvent(event));
        }
        mHandler.post(new EventRunnable(event,id));
        lastSlotEvent.put(id,event);
    }
}
