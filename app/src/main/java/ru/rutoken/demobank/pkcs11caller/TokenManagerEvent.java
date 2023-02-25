package ru.rutoken.demobank.pkcs11caller;

import androidx.annotation.Nullable;

import com.sun.jna.NativeLong;

import java.util.Objects;

import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO;

/**
 * Event structure, field contents depend on event type.
 */
class TokenManagerEvent {
    final EventType type;
    @Nullable
    private final SlotEventThread.SlotEvent slotEvent;
    @Nullable
    private final CK_TOKEN_INFO tokenInfo;
    @Nullable
    private final Token token;
    @Nullable
    private final Pkcs11CallerException exception;

    TokenManagerEvent(EventType type) {
        this(type, null, null, null, null);
    }

    TokenManagerEvent(EventType type, SlotEventThread.SlotEvent slotEvent) {
        this(type, slotEvent, null, null, null);
    }

    TokenManagerEvent(EventType type, SlotEventThread.SlotEvent slotEvent,
                      CK_TOKEN_INFO tokenInfo) {
        this(type, slotEvent, tokenInfo, null, null);
    }

    TokenManagerEvent(EventType type, SlotEventThread.SlotEvent slotEvent, Token token) {
        this(type, slotEvent, null, token, null);
    }

    TokenManagerEvent(EventType type, SlotEventThread.SlotEvent slotEvent,
                      Pkcs11CallerException exception) {
        this(type, slotEvent, null, null, exception);
    }

    private TokenManagerEvent(EventType type, @Nullable SlotEventThread.SlotEvent slotEvent,
                              @Nullable CK_TOKEN_INFO tokenInfo, @Nullable Token token,
                              @Nullable Pkcs11CallerException exception) {
        this.type = Objects.requireNonNull(type);
        this.slotEvent = slotEvent;
        this.tokenInfo = tokenInfo;
        this.token = token;
        this.exception = exception;
    }

    SlotEventThread.SlotEvent requireSlotEvent() {
        return Objects.requireNonNull(slotEvent);
    }

    NativeLong requireSlotId() {
        return requireSlotEvent().slotId;
    }

    CK_TOKEN_INFO requireTokenInfo() {
        return Objects.requireNonNull(tokenInfo);
    }

    Token requireToken() {
        return Objects.requireNonNull(token);
    }

    Pkcs11CallerException requireException() {
        return Objects.requireNonNull(exception);
    }

    enum EventType {
        SLOT_ADDED,
        SLOT_REMOVED,
        TOKEN_INFO_LOADED,
        TOKEN_INFO_FAILED,
        TOKEN_ADDED,
        TOKEN_ADDING_FAILED,
        ENUMERATION_FINISHED,
        SLOT_EVENT_THREAD_FAILED
    }
}
