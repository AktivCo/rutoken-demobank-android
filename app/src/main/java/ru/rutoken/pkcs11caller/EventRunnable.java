/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.NativeLong;

class EventRunnable implements Runnable {
    private final EventType mEvent;
    private final NativeLong mSlotId;
    private Token mToken = null;

    EventRunnable(EventType event, NativeLong slotId) {
        mEvent = event;
        mSlotId = slotId;
    }

    EventRunnable(EventType event, NativeLong slotId, Token token) {
        this(event, slotId);
        mToken = token;
    }

    @Override
    public void run() {
        TokenManager.getInstance().processEvent(mEvent, mSlotId, mToken);
    }
}
