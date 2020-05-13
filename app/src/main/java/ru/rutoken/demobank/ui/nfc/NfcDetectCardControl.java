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

    final MutableLiveData<Boolean> mWorkProgressFlag = new MutableLiveData<>();
    final MutableLiveData<Void> mDismissAction = new MutableLiveData<>();
    @Nullable
    final NfcDetectCardControl.CancelCallback mCancelCallback;
    private final int mId;
    private final FragmentManager mFragmentManager;

    public NfcDetectCardControl(FragmentManager fragmentManager, @Nullable CancelCallback cancelCallback) {
        mFragmentManager = Objects.requireNonNull(fragmentManager);
        mCancelCallback = cancelCallback;

        sControls.put(mId = sIdCounter.getAndIncrement(), this);
    }

    static NfcDetectCardControl takeControl(int id) {
        return Objects.requireNonNull(sControls.remove(id));
    }

    public void show() {
        NfcDetectCardFragment.newInstance(mId).show(mFragmentManager, "nfcDetectCard");
    }

    public void startWorkProgress() {
        mWorkProgressFlag.postValue(true);
    }

    public void dismiss() {
        mDismissAction.postValue(null);
    }

    /**
     * Interface to receive cancellation use action
     */
    @FunctionalInterface
    public interface CancelCallback {
        void onCancel();
    }
}
