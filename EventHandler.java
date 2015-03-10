/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11.CK_C_INITIALIZE_ARGS;
import ru.rutoken.Pkcs11.CK_SLOT_INFO;
import ru.rutoken.Pkcs11.Pkcs11Constants;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;

public class EventHandler extends Thread {
    public EventHandler() {
    }

    private Map<NativeLong, EventType> lastSlotEvent = new HashMap<NativeLong, EventType>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void run() {
        try {
            NativeLong rv;

            CK_C_INITIALIZE_ARGS initializeArgs = new CK_C_INITIALIZE_ARGS(null, null, null, null, Pkcs11Constants.CKF_OS_LOCKING_OK, null);
            rv = RtPkcs11Library.getInstance().C_Initialize(initializeArgs);

            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }

            NativeLongByReference slotCount = new NativeLongByReference(new NativeLong(0));
            rv = RtPkcs11Library.getInstance().C_GetSlotList(false, null, slotCount);

            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }

            NativeLong slotIds[] = new NativeLong[slotCount.getValue().intValue()];
            rv = RtPkcs11Library.getInstance().C_GetSlotList(true, slotIds, slotCount);

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

        while (true) {
            NativeLongByReference id = new NativeLongByReference();
            NativeLong rv;

            rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(new NativeLong(0), id, null);

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
            }
        }
    }

    static EventType oppositeEvent(EventType event) {
        if (event == EventType.SD) return EventType.SR;
        return EventType.SD;
    }

    protected void slotEventHappened(NativeLong id) throws Pkcs11Exception {
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        NativeLong rv;
        rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);

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
            mHandler.post(new EventRunnable(oppositeEvent(event), id));
            lastSlotEvent.put(id, oppositeEvent(event));
        }
        mHandler.post(new EventRunnable(event, id));
        lastSlotEvent.put(id, event);
    }
}
