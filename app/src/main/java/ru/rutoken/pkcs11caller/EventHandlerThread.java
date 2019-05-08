/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.pkcs11jna.CK_C_INITIALIZE_ARGS;
import ru.rutoken.pkcs11jna.CK_SLOT_INFO;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

class EventHandlerThread extends Thread {
    EventHandlerThread() {
        setName(getClass().getName());
    }

    private final Map<NativeLong, EventType> mLastSlotEvent = new HashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void run() {
        try {
            NativeLong rv;

            CK_C_INITIALIZE_ARGS initializeArgs = new CK_C_INITIALIZE_ARGS(null, null,
                    null, null, new NativeLong(Pkcs11Constants.CKF_OS_LOCKING_OK), null);
            rv = RtPkcs11Library.getInstance().C_Initialize(initializeArgs);
            Pkcs11Exception.throwIfNotOk(rv);

            NativeLongByReference slotCount = new NativeLongByReference(new NativeLong(0));
            rv = RtPkcs11Library.getInstance().C_GetSlotList(Pkcs11Constants.CK_FALSE, null, slotCount);
            Pkcs11Exception.throwIfNotOk(rv);

            NativeLong slotIds[] = new NativeLong[slotCount.getValue().intValue()];
            rv = RtPkcs11Library.getInstance().C_GetSlotList(Pkcs11Constants.CK_TRUE, slotIds, slotCount);
            Pkcs11Exception.throwIfNotOk(rv);

            for (int i = 0; i != slotCount.getValue().intValue(); ++i) {
                slotEventHappened(slotIds[i]);
            }

            mHandler.post(new EventRunnable(EventType.ENUMERATION_FINISHED, new NativeLong(-1)));
        } catch (Exception e) {
            mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED, new NativeLong(-1)));
        }

        while (!isInterrupted()) {
            NativeLongByReference id = new NativeLongByReference();
            NativeLong rv;

            rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(new NativeLong(0), id, null);

            if (rv.longValue() == Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED) {
                Log.d(getClass().getName(), "Exit " + getClass().getSimpleName());
                return;
            }

            try {
                Pkcs11Exception.throwIfNotOk(rv);
                slotEventHappened(id.getValue());
            } catch (Exception e) {
                mHandler.post(new EventRunnable(EventType.EVENT_HANDLER_FAILED, new NativeLong(-1)));
            }
        }
    }

    private static EventType oppositeEvent(EventType event) {
        if (event == EventType.SLOT_ADDED) return EventType.SLOT_REMOVED;
        return EventType.SLOT_ADDED;
    }

    private void slotEventHappened(NativeLong id) throws Pkcs11Exception {
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        NativeLong rv;
        rv = RtPkcs11Library.getInstance().C_GetSlotInfo(id, slotInfo);
        Pkcs11Exception.throwIfNotOk(rv);

        EventType event;
        if ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.flags.longValue()) != 0x00) {
            event = EventType.SLOT_ADDED;
        } else {
            event = EventType.SLOT_REMOVED;
        }

        if (mLastSlotEvent.get(id) == event) {
            mHandler.post(new EventRunnable(oppositeEvent(event), id));
            mLastSlotEvent.put(id, oppositeEvent(event));
        }
        mHandler.post(new EventRunnable(event, id));
        mLastSlotEvent.put(id, event);
    }
}
