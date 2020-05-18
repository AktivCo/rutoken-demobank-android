package ru.rutoken.demobank.ui.nfc;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

class NfcDetectCardViewModel extends ViewModel {
    private final NfcDetectCardControl mControl;

    NfcDetectCardViewModel(int controlId) {
        mControl = NfcDetectCardControl.takeControl(controlId);
    }

    LiveData<Boolean> getWorkProgressFlag() {
        return mControl.getData().workProgressFlag;
    }

    LiveData<Void> getDismissAction() {
        return mControl.getData().dismissAction;
    }

    void cancel() {
        if (mControl.getData().cancelCallback != null)
            mControl.getData().cancelCallback.onCancel();
    }
}
