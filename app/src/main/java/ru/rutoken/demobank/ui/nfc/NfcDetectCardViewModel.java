package ru.rutoken.demobank.ui.nfc;

import androidx.annotation.AnyThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ru.rutoken.demobank.pkcs11caller.TokenExecutors;
import ru.rutoken.demobank.pkcs11caller.TokenManager;

class NfcDetectCardViewModel extends ViewModel {
    private final MutableLiveData<Command> mCommand = new MutableLiveData<>();
    private final String mTokenSerial;

    private Future mTokenWaitTask;


    NfcDetectCardViewModel(String tokenSerial) {
        mTokenSerial = Objects.requireNonNull(tokenSerial);
    }

    LiveData<Command> getCommand() {
        return mCommand;
    }

    @AnyThread
    void setCommand(Command command) {
        mCommand.postValue(command);
    }

    void startTokenWait() {
        mTokenWaitTask = TokenExecutors.getInstance().get(mTokenSerial).submit(() -> {
            try {
                TokenManager.getInstance().getSlotIdByTokenSerial(mTokenSerial).get();
                setCommand(Command.SHOW_PROGRESS);
            } catch (ExecutionException | InterruptedException | CancellationException e) {
                setCommand(Command.DISMISS);
                e.printStackTrace();
            }
        });
    }

    void stopTokenWait() {
        if (!(Objects.requireNonNull(mTokenWaitTask)).isDone())
            mTokenWaitTask.cancel(true);
    }

    enum Command {
        SHOW_PROGRESS,
        DISMISS
    }
}
