/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.pkcs11caller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.SettableFuture;
import com.sun.jna.NativeLong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import ru.rutoken.demobank.pkcs11caller.TokenManagerEvent.EventType;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO;
import ru.rutoken.demobank.utils.KeyExecutors;

import static ru.rutoken.demobank.pkcs11caller.SlotEventThread.SlotEvent;
import static ru.rutoken.demobank.pkcs11caller.TokenManagerEvent.EventType.SLOT_ADDED;

public class TokenManager implements DefaultLifecycleObserver {
    private static final TokenManager INSTANCE = new TokenManager();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final KeyExecutors<TokenData> EXECUTORS = new KeyExecutors<>(Executors::newSingleThreadExecutor);

    private final Set<TokenData> mTokenDataSet = Collections.synchronizedSet(new HashSet<>());
    private final CopyOnWriteArraySet<Listener> mListeners = new CopyOnWriteArraySet<>();
    private SlotEventThread mSlotEventThread;

    public static TokenManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        mSlotEventThread = new SlotEventThread();
        mSlotEventThread.start();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mSlotEventThread.interrupt();
        final Iterator<TokenData> iterator = mTokenDataSet.iterator();
        while (iterator.hasNext()) {
            final TokenData tokenData = iterator.next();
            final String tokenSerial = tokenData.getSerialNumber();
            tokenData.close();
            iterator.remove();

            if (tokenSerial != null)
                notifyTokenRemoved(tokenSerial);
        }
        RtPkcs11Library.getInstance().C_Finalize(null);
    }

    public void addListener(Listener listener) {
        mListeners.add(Objects.requireNonNull(listener));
    }

    @AnyThread
    void postEvent(TokenManagerEvent event) {
        HANDLER.post(() -> processEvent(event));
    }

    @AnyThread
    public List<String> getTokenSerials() {
        final List<String> tokenSerials = new ArrayList<>();
        synchronized (mTokenDataSet) {
            for (TokenData tokenData : mTokenDataSet) {
                final String serial = tokenData.getSerialNumber();
                if (serial != null)
                    tokenSerials.add(serial);
            }
        }
        return tokenSerials;
    }

    @AnyThread
    @Nullable
    public Token getTokenBySerial(String tokenSerial) {
        final TokenData tokenData = findTokenDataBySerial(tokenSerial);
        if (tokenData != null)
            return tokenData.token;
        return null;
    }

    /**
     * @return Future with slot id, getting its result may block if token is not available,
     * for example NFC card is out of range.
     */
    @AnyThread
    public Future<NativeLong> getSlotIdByTokenSerial(String tokenSerial) {
        final TokenData tokenData = findTokenDataBySerial(tokenSerial);
        if (tokenData == null)
            throw new IllegalStateException("Token not found, serial: " + tokenSerial);
        return tokenData.slotId.get();
    }

    @AnyThread
    public void removeNfcToken(String tokenSerial) {
        final TokenData tokenData = findTokenDataBySerial(tokenSerial);
        if (tokenData == null)
            throw new IllegalStateException("Token not found for serial: " + tokenSerial);
        else if (!tokenData.isNfc)
            throw new IllegalArgumentException("Token with serial: " + tokenSerial + " is not NFC card");

        if (tokenData.token == null)
            throw new IllegalStateException("Token with serial: " + tokenSerial + " is not constructed yet");

        removeTokenData(tokenData);
        notifyTokenRemoved(tokenSerial);
    }

    @MainThread
    private void processEvent(TokenManagerEvent event) {
        try {
            switch (event.type) {
                case SLOT_EVENT_THREAD_FAILED:
                    notifyListeners(Listener::onInternalError);
                    break;
                case ENUMERATION_FINISHED:
                    notifyListeners(Listener::onEnumerationFinished);
                    break;
                default:
                    TokenData tokenData = findTokenDataBySlotId(event.requireSlotId());
                    if (tokenData == null) {
                        if (event.type != SLOT_ADDED) {
                            Log.v(getClass().getName(), "Can not find TokenData to handle event: " + event.type +
                                    ", slot: " + event.requireSlotId().longValue());
                            return;
                        }
                        tokenData = new TokenData(event.requireSlotEvent());
                        Log.v(getClass().getName(), "creating " + tokenData);
                        mTokenDataSet.add(tokenData);
                    }
                    tokenData.processEvent(event);
                    break;
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), "Event " + event.type + " processing error", e);
        }
    }

    @Nullable
    private TokenData findTokenDataBySerial(String tokenSerial) {
        synchronized (mTokenDataSet) {
            for (TokenData tokenData : mTokenDataSet)
                if (tokenData.token != null && tokenData.token.getSerialNumber().equals(tokenSerial))
                    return tokenData;
        }
        return null;
    }

    @Nullable
    private TokenData findTokenDataBySlotId(NativeLong slotId) {
        synchronized (mTokenDataSet) {
            for (TokenData tokenData : mTokenDataSet) {
                try {
                    final Future<NativeLong> slotIdFuture = tokenData.slotId.get();
                    if (slotIdFuture.isDone() && !slotIdFuture.isCancelled() && slotIdFuture.get().equals(slotId))
                        return tokenData;
                } catch (InterruptedException | ExecutionException ignore) {
                    // slot id future cannot be finished with exception
                }
            }
        }
        return null;
    }

    private void removeTokenData(TokenData tokenData) {
        Log.v(getClass().getName(), "removing " + tokenData);
        mTokenDataSet.remove(tokenData);
        tokenData.close();
    }

    private void notifyListeners(Consumer<Listener> consumer) {
        for (Listener listener : mListeners)
            consumer.accept(listener);
    }

    private void notifyTokenAdding(NativeLong slotId) {
        notifyListeners(listener -> listener.onTokenAdding(slotId.longValue()));
    }

    private void notifyTokenAddingFailed(NativeLong slotId, String errorMessage) {
        notifyListeners(listener -> listener.onTokenAddingFailed(slotId.longValue(), errorMessage));
    }

    private void notifyTokenAdded(String tokenSerial) {
        notifyListeners(listener -> listener.onTokenAdded(tokenSerial));
    }

    private void notifyTokenRemoved(String tokenSerial) {
        notifyListeners(listener -> listener.onTokenRemoved(tokenSerial));
    }

    public interface Listener {
        void onEnumerationFinished();

        void onTokenAdding(long slotId);

        void onTokenAddingFailed(long slotId, String error);

        void onTokenAdded(String tokenSerial);

        void onTokenRemoved(String tokenSerial);

        void onInternalError();
    }

    private static class TokenManagerException extends RuntimeException {
        TokenManagerException(String what) {
            super(what);
        }
    }

    /**
     * The structure to store data associated with a token.
     * Note that a NFC card with the same serial number may be present in different slots at different moments of time.
     */
    private static class TokenData implements AutoCloseable {
        /**
         * On remove event there is not slotInfo so we store this data as part of our state.
         */
        final boolean isNfc;
        @Nullable
        Token token = null;
        private AtomicReference<SettableFuture<NativeLong>> slotId = new AtomicReference<>();
        private State state = State.SlotRemoved;

        TokenData(SlotEvent slotEvent) {
            isNfc = new String(slotEvent.slotInfo.slotDescription).contains("Aktiv Rutoken ECP NFC");
            final SettableFuture<NativeLong> slotIdFuture = SettableFuture.create();
            slotIdFuture.set(slotEvent.slotId);
            slotId.set(slotIdFuture);
        }

        void resetSlotId() {
            final Future<NativeLong> slotIdFuture = slotId.getAndSet(SettableFuture.create());
            slotIdFuture.cancel(true);
        }

        @Nullable
        String getSerialNumber() {
            return token != null ? token.getSerialNumber() : null;
        }

        @MainThread
        void processEvent(TokenManagerEvent event) {
            if (state == State.Closed) {
                Log.v(getClass().getName(), this + " is closed, event: " + event.type +
                        " is rejected, slot: " + event.requireSlotId().longValue());
                return;
            }
            try {
                Log.v(getClass().getName(), this + " current state: " + state + ", processing event: " + event.type +
                        ", slot: " + event.requireSlotId().longValue());

                setState(state.process(event, this, INSTANCE), event);
            } catch (Exception e) {
                Log.w(getClass().getName(), "Event " + event.type + " processing error", e);
            }
        }

        private void setState(State state, TokenManagerEvent event) {
            if (this.state == State.Closed) {
                if (state == State.Closed)
                    return;
                throw new IllegalStateException(this + " is closed, and cannot accept state: " + state);
            }

            this.state = state;
            Log.v(getClass().getName(),
                    this + " going to state: " + state + ", slot: " + event.requireSlotId().longValue());
        }

        @NonNull
        @Override
        public String toString() {
            final String tokenSerial = getSerialNumber();
            return getClass().getSimpleName() + " " + hashCode()
                    + (tokenSerial != null ? ", tokenSerial: " + tokenSerial : "");
        }

        @AnyThread
        void postEvent(TokenManagerEvent event) {
            HANDLER.post(() -> processEvent(event));
        }

        /**
         * Closing TokenData prevents possibility of processing events, from hanged working threads.
         */
        @Override
        public void close() {
            state = State.Closed;
            resetSlotId();
        }

        private enum State {
            SlotRemoved() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    // On adding nfc card there already might exist TokenData
                    if (event.type == SLOT_ADDED) {
                        TokenInfoLoader.start(event.requireSlotEvent(), tokenData);
                        return State.TokenInfoLoading;
                    }
                    throw new TokenManagerException(
                            "Unexpected unfiltered incoming event: " + event.type + ", state: " + this);
                }
            },
            TokenInfoLoading() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    switch (event.type) {
                        case SLOT_REMOVED:
                            tokenManager.removeTokenData(tokenData);
                            return State.Closed;
                        case TOKEN_INFO_LOADED:
                            if (tokenData.isNfc) {
                                // check if we already have TokenData for this card
                                final TokenData tokenDataBySerial = tokenManager.findTokenDataBySerial(
                                        Utils.removeTrailingSpaces((event.requireTokenInfo().serialNumber)));
                                // card may appear in different slot
                                if (tokenDataBySerial != null && tokenData != tokenDataBySerial) {
                                    tokenManager.removeTokenData(tokenData);
                                    tokenDataBySerial.slotId.get().set(event.requireSlotId());
                                    tokenDataBySerial.setState(this, event);
                                    Log.v(getClass().getName(), "passing event " + event.type +
                                            " from " + tokenData + " to " + tokenDataBySerial);
                                    tokenDataBySerial.processEvent(event);
                                    return State.Closed;
                                }

                                // Token instance may be already present
                                if (tokenData.token != null)
                                    return State.TokenAdded;
                            }
                            tokenManager.notifyTokenAdding(event.requireSlotId());
                            TokenCreator.start(event.requireSlotEvent(), event.requireTokenInfo(), tokenData);
                            return State.TokenAdding;
                        case TOKEN_INFO_FAILED:
                            return State.TokenAddingFailed;
                        default:
                            throw new TokenManagerException(
                                    "Unexpected unfiltered incoming event: " + event.type + ", state: " + this);
                    }
                }
            },
            TokenAdding() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    switch (event.type) {
                        case SLOT_REMOVED:
                            tokenManager.notifyTokenAddingFailed(
                                    event.requireSlotId(), "Slot removed during token adding");
                            tokenManager.removeTokenData(tokenData);
                            return State.Closed;
                        case TOKEN_ADDED:
                            // Token created
                            tokenData.token = event.requireToken();
                            tokenManager.notifyTokenAdded(event.requireToken().getSerialNumber());
                            return State.TokenAdded;
                        case TOKEN_ADDING_FAILED:
                            tokenManager.notifyTokenAddingFailed(event.requireSlotId(),
                                    event.requireException().getMessage());
                            return State.TokenAddingFailed;
                        default:
                            throw new TokenManagerException(
                                    "Unexpected unfiltered incoming event: " + event.type + ", state: " + this);
                    }
                }
            },
            TokenAdded() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    if (event.type == EventType.SLOT_REMOVED) {
                        if (tokenData.isNfc) { // hide event for NFC cards
                            // now slot is unknown for Token
                            tokenData.resetSlotId();
                            return State.SlotRemoved;
                        }
                        tokenManager.removeTokenData(tokenData);
                        tokenManager.notifyTokenRemoved(Objects.requireNonNull(tokenData.getSerialNumber()));
                        return State.Closed;
                    }
                    throw new TokenManagerException(
                            "Unexpected unfiltered incoming event: " + event.type + ", state: " + this);
                }
            },
            TokenAddingFailed() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    if (event.type == EventType.SLOT_REMOVED) {
                        tokenManager.removeTokenData(tokenData);
                        return State.Closed;
                    }
                    throw new TokenManagerException(
                            "Unexpected unfiltered incoming event: " + event.type + ", state: " + this);
                }
            },
            Closed() {
                @Override
                State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager) {
                    throw new IllegalStateException("Closed object cannot handle events");
                }
            };

            abstract State process(TokenManagerEvent event, TokenData tokenData, TokenManager tokenManager);
        }
    }

    private static class TokenCreator {
        static void start(SlotEvent slotEvent, CK_TOKEN_INFO tokenInfo, TokenData tokenData) {
            EXECUTORS.get(tokenData).execute(() -> {
                try {
                    final Token token = new Token(slotEvent.slotId, tokenInfo,
                            tokenData.isNfc, RtPkcs11Library.getInstance());

                    tokenData.postEvent(new TokenManagerEvent(EventType.TOKEN_ADDED, slotEvent, token));
                } catch (Pkcs11CallerException e) {
                    e.printStackTrace();
                    tokenData.postEvent(new TokenManagerEvent(EventType.TOKEN_ADDING_FAILED, slotEvent, e));
                }
            });
        }
    }

    private static class TokenInfoLoader {
        static void start(SlotEvent slotEvent, TokenData tokenData) {
            EXECUTORS.get(tokenData).execute(() -> {
                try {
                    final CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
                    Pkcs11Exception.throwIfNotOk(
                            RtPkcs11Library.getInstance().C_GetTokenInfo(slotEvent.slotId, tokenInfo));

                    tokenData.postEvent(new TokenManagerEvent(EventType.TOKEN_INFO_LOADED, slotEvent, tokenInfo));
                } catch (Pkcs11CallerException e) {
                    e.printStackTrace();
                    tokenData.postEvent(new TokenManagerEvent(EventType.TOKEN_INFO_FAILED, slotEvent, e));
                }
            });
        }
    }
}
