package ru.rutoken.demobank.ui.nfc;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class NfcDetectCardViewModel extends ViewModel {
    private final NfcDetectCardControl mControl;

    NfcDetectCardViewModel(int controlId) {
        mControl = NfcDetectCardControl.takeControl(controlId);
    }

    LiveData<Boolean> getWorkProgressFlag() {
        return mControl.mWorkProgressFlag;
    }

    LiveData<Void> getDismissAction() {
        return mControl.mDismissAction;
    }

    void cancel() {
        if (mControl.mCancelCallback != null)
            mControl.mCancelCallback.onCancel();
    }
}
