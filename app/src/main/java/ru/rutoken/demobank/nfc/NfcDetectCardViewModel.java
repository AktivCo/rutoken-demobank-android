package ru.rutoken.demobank.nfc;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sun.jna.NativeLong;

import java.util.concurrent.ExecutionException;

import ru.rutoken.pkcs11caller.TokenExecutors;
import ru.rutoken.pkcs11caller.TokenManager;

class NfcDetectCardViewModel extends ViewModel {
    private MutableLiveData<Command> mCommand = new MutableLiveData<>();

    NfcDetectCardViewModel(String tokenSerial) {
        //TODO: use normal key for TokenExecutors
        TokenExecutors.getInstance().get(new NativeLong(1)).execute(() -> {
            try {
                TokenManager.getInstance().getSlotIdByTokenSerial(tokenSerial).get();
                mCommand.postValue(Command.SHOW_PROGRESS);
            } catch (ExecutionException | InterruptedException e) {
                mCommand.postValue(Command.DISMISS);
                e.printStackTrace();
            }
        });
    }

    LiveData<Command> getCommand() {
        return mCommand;
    }

    void setCommand(Command command) {
        mCommand.postValue(command);
    }

    enum Command {
        SHOW_PROGRESS,
        DISMISS
    }
}
