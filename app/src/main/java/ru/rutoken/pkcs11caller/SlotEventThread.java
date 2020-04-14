/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import android.util.Log;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ru.rutoken.pkcs11caller.TokenManagerEvent.EventType;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_C_INITIALIZE_ARGS;
import ru.rutoken.pkcs11jna.CK_SLOT_INFO;
import ru.rutoken.pkcs11jna.Pkcs11Constants;

import static ru.rutoken.pkcs11caller.TokenManagerEvent.EventType.ENUMERATION_FINISHED;
import static ru.rutoken.pkcs11caller.TokenManagerEvent.EventType.SLOT_EVENT_THREAD_FAILED;
import static ru.rutoken.pkcs11caller.TokenManagerEvent.EventType.SLOT_ADDED;
import static ru.rutoken.pkcs11caller.TokenManagerEvent.EventType.SLOT_REMOVED;

class SlotEventThread extends Thread {
    private final Map<NativeLong, EventType> mLastSlotEvent = new HashMap<>();

    SlotEventThread() {
        setName(getClass().getName());
    }

    private static EventType oppositeEvent(EventType event) {
        return event == SLOT_ADDED ? SLOT_REMOVED : SLOT_ADDED;
    }

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

            NativeLong[] slotIds = new NativeLong[slotCount.getValue().intValue()];
            rv = RtPkcs11Library.getInstance().C_GetSlotList(Pkcs11Constants.CK_TRUE, slotIds, slotCount);
            Pkcs11Exception.throwIfNotOk(rv);

            for (int i = 0; i != slotCount.getValue().intValue(); ++i) {
                slotEventHappened(slotIds[i]);
            }

            TokenManager.getInstance().postEvent(new TokenManagerEvent(ENUMERATION_FINISHED));
        } catch (Exception e) {
            TokenManager.getInstance().postEvent(new TokenManagerEvent(SLOT_EVENT_THREAD_FAILED));
        }

        while (!isInterrupted()) {
            NativeLongByReference slotId = new NativeLongByReference();
            NativeLong rv;

            rv = RtPkcs11Library.getInstance().C_WaitForSlotEvent(new NativeLong(0), slotId, null);

            if (rv.longValue() == Pkcs11Constants.CKR_CRYPTOKI_NOT_INITIALIZED) {
                Log.d(getClass().getName(), "Exit " + getClass().getSimpleName());
                return;
            }

            try {
                Pkcs11Exception.throwIfNotOk(rv);
                slotEventHappened(slotId.getValue());
            } catch (Exception e) {
                TokenManager.getInstance().postEvent(new TokenManagerEvent(SLOT_EVENT_THREAD_FAILED));
            }
        }
    }

    private void slotEventHappened(NativeLong slotId) throws Pkcs11Exception {
        CK_SLOT_INFO slotInfo = new CK_SLOT_INFO();
        NativeLong rv;
        rv = RtPkcs11Library.getInstance().C_GetSlotInfo(slotId, slotInfo);
        Pkcs11Exception.throwIfNotOk(rv);

        final EventType eventType;
        if ((Pkcs11Constants.CKF_TOKEN_PRESENT & slotInfo.flags.longValue()) != 0x00) {
            eventType = SLOT_ADDED;
        } else {
            eventType = SLOT_REMOVED;
        }

        if (mLastSlotEvent.get(slotId) == eventType) {
            final EventType oppositeEventType = oppositeEvent(eventType);
            Log.v(getClass().getName(), "post opposite (virtual) event: " + oppositeEventType + ", slot: " + slotId.longValue());
            TokenManager.getInstance().postEvent(new TokenManagerEvent(oppositeEventType, new SlotEvent(slotId, slotInfo)));
            mLastSlotEvent.put(slotId, oppositeEventType);
        }

        Log.v(getClass().getName(), "post event: " + eventType + ", slot: " + slotId.longValue());
        TokenManager.getInstance().postEvent(new TokenManagerEvent(eventType, new SlotEvent(slotId, slotInfo)));
        mLastSlotEvent.put(slotId, eventType);
    }

    static class SlotEvent {
        final NativeLong slotId;
        final CK_SLOT_INFO slotInfo;

        SlotEvent(NativeLong slotId, CK_SLOT_INFO slotInfo) {
            this.slotId = Objects.requireNonNull(slotId);
            this.slotInfo = Objects.requireNonNull(slotInfo);
        }
    }
}
