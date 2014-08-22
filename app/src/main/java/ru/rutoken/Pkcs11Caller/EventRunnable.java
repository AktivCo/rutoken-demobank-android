package ru.rutoken.Pkcs11Caller;

/**
 * Created by mironenko on 21.08.2014.
 */
public class EventRunnable implements Runnable {
    EventType mEvent;
    int mSlotId;
    Token mToken = null;
    EventRunnable(EventType event, int slotId) {
        mEvent = event;
        mSlotId = slotId;
    }
    EventRunnable(EventType event, int slotId, Token token) {
        this(event, slotId);
        mToken = token;
    }
    public void run() {
        TokenManager.getInstance().processEvent(mEvent, mSlotId, mToken);
    }
}
