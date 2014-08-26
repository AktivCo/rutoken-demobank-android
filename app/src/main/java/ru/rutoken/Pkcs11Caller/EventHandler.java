package ru.rutoken.Pkcs11Caller;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

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
    Map<NativeLong, EventType> lastSlotEvent = new HashMap<NativeLong, EventType>();
    Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void run() {
        try {
            NativeLong rv;
            synchronized (RtPkcs11Library.getInstance()) {
                 rv = RtPkcs11Library.getInstance().C_Initialize(null);
            }
            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }

            NativeLongByReference slotCount = new NativeLongByReference(new NativeLong(0));
            synchronized (RtPkcs11Library.getInstance()) {
                rv = RtPkcs11Library.getInstance().C_GetSlotList(false, null, slotCount);
            }
            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            NativeLong slotIds[] = new NativeLong[slotCount.getValue().intValue()];
            synchronized (RtPkcs11Library.getInstance()) {
                rv = RtPkcs11Library.getInstance().C_GetSlotList(true, slotIds, slotCount);
            }
            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            for (int i = 0; i != slotCount.getValue().intValue(); ++i) {
                slotEventHappened(slotIds[i]);
            }
            mHandler.post(new EventRunnable(EventType.ENUMERATION_FINISHED, new NativeLong(-1)));
        } catch (Exception e) {
            mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED, new NativeLong(-1)));
        }

        while(true) {
            NativeLongByReference id = new NativeLongByReference();
            NativeLong rv;
            do {
                synchronized (RtPkcs11Library.getInstance()) {
                    rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(Pkcs11Constants.CKF_DONT_BLOCK, id, null);
                }
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
            } while (rv.equals(Pkcs11Constants.CKR_NO_EVENT));
            if (rv.equals(Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED)) {
                Log.d(getClass().getName(), "Exit EH");
                return;
            }
            try {
                if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                    throw Pkcs11Exception.exceptionWithCode(rv);
                }
                slotEventHappened(id.getValue());
            } catch (Exception e) {
                mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED, new NativeLong(-1)));
                // TODO ??? return ???
            }
        }
    }

    static EventType oppositeEvent(EventType event) {
        if(event == EventType.SD) return EventType.SR;
        return EventType.SD;
    }

    protected void slotEventHappened(NativeLong id) throws Pkcs11Exception {
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        NativeLong rv;
        synchronized (RtPkcs11Library.getInstance()) {
            rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);
        }
        if (!rv.equals(Pkcs11Constants.CKR_OK)) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }

        EventType event;
        if ((Pkcs11Constants.CKF_TOKEN_PRESENT.intValue() & slotInfo.flags.intValue()) != 0x00) {
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
