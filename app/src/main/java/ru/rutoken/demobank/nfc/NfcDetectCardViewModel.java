package ru.rutoken.demobank.nfc;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sun.jna.NativeLong;

import java.util.concurrent.ExecutionException;

import ru.rutoken.pkcs11caller.TokenExecutors;
import ru.rutoken.pkcs11caller.TokenManager;

public class NfcDetectCardViewModel extends ViewModel {

    private MutableLiveData<Boolean> mProgress = new MutableLiveData<>();

    public NfcDetectCardViewModel(String tokenSerial) {
        TokenManager.getInstance().getSlotIdByTokenSerial(tokenSerial);
        //TODO: use normal key for TokenExecutors
        TokenExecutors.getInstance().get(new NativeLong(1)).execute(() -> {
            try {
                TokenManager.getInstance().getSlotIdByTokenSerial(tokenSerial).get();
                mProgress.postValue(true);
            } catch (ExecutionException | InterruptedException e) {
                mProgress.postValue(false);
                e.printStackTrace();
            }
        });
    }

    public LiveData<Boolean> getProgress() {
        return mProgress;
    }
}
