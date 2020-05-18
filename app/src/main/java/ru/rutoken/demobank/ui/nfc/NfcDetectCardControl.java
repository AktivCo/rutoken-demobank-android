package ru.rutoken.demobank.ui.nfc;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is designed to show and interact with NfcDetectCardFragment from working thread.
 */
@AnyThread
public class NfcDetectCardControl {
    private final static AtomicInteger sIdCounter = new AtomicInteger();
    private final static Map<Integer /* id */, NfcDetectCardControl> sControls =
            Collections.synchronizedMap(new HashMap<>());

    private final FragmentManager mFragmentManager;
    private final Data mData;
    private final int mId;

    public NfcDetectCardControl(FragmentManager fragmentManager, @Nullable CancelCallback cancelCallback) {
        mFragmentManager = Objects.requireNonNull(fragmentManager);
        mData = new Data(cancelCallback);

        sControls.put(mId = sIdCounter.getAndIncrement(), this);
    }

    static NfcDetectCardControl takeControl(int id) {
        return Objects.requireNonNull(sControls.remove(id));
    }

    public void show() {
        NfcDetectCardFragment.newInstance(mId).show(mFragmentManager, "nfcDetectCard");
    }

    public void startWorkProgress() {
        mData.workProgressFlag.postValue(true);
    }

    public void dismiss() {
        mData.dismissAction.postValue(null);
    }

    Data getData() {
        return mData;
    }

    /**
     * Interface to receive cancellation use action
     */
    @FunctionalInterface
    public interface CancelCallback {
        void onCancel();
    }

    static class Data {
        final MutableLiveData<Boolean> workProgressFlag = new MutableLiveData<>();
        final MutableLiveData<Void> dismissAction = new MutableLiveData<>();
        @Nullable
        final NfcDetectCardControl.CancelCallback cancelCallback;

        Data(@Nullable CancelCallback cancelCallback) {
            this.cancelCallback = cancelCallback;
        }
    }
}
