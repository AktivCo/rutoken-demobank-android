package ru.rutoken.Pkcs11Caller;

import com.sun.jna.NativeLong;

public class EventRunnable implements Runnable {
    EventType mEvent;
    NativeLong mSlotId;
    Token mToken = null;

    EventRunnable(EventType event, NativeLong slotId) {
        mEvent = event;
        mSlotId = slotId;
    }

    EventRunnable(EventType event, NativeLong slotId, Token token) {
        this(event, slotId);
        mToken = token;
    }

    public void run() {
        TokenManager.getInstance().processEvent(mEvent, mSlotId, mToken);
    }
}
