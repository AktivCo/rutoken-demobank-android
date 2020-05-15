package ru.rutoken.demobank.ui.nfc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.Factory;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ru.rutoken.demobank.R;

import static ru.rutoken.demobank.ui.nfc.NfcDetectCardViewModel.Command;

public class NfcDetectCardFragment extends BottomSheetDialogFragment {
    private static final String TOKEN_SERIAL_KEY = "tokenSerialKey";

    private NfcDetectCardViewModel mViewModel;
    private CancelCallback mCancelCallback;

    public static NfcDetectCardFragment newInstance(String tokenSerial) {
        Bundle bundle = new Bundle();
        bundle.putString(TOKEN_SERIAL_KEY, Objects.requireNonNull(tokenSerial));
        NfcDetectCardFragment nfcDetectCardFragment = new NfcDetectCardFragment();
        nfcDetectCardFragment.setArguments(bundle);

        return nfcDetectCardFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        return inflater.inflate(R.layout.nfc_detect_card_fragment, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewById(R.id.nfcCancelButton).setOnClickListener((View v) -> dismiss());
        String tokenSerial = Objects.requireNonNull(getArguments()).getString(TOKEN_SERIAL_KEY);

        mViewModel = new ViewModelProvider(this, new Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T) new NfcDetectCardViewModel(tokenSerial);
            }
        }).get(NfcDetectCardViewModel.class);

        mViewModel.getCommand().observe(getViewLifecycleOwner(), this::onCommand);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //TODO: Find a proper way to set animation
        Objects.requireNonNull(
                Objects.requireNonNull(getDialog())
                        .getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onStart() {
        mViewModel.startTokenWait();
        super.onStart();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        mViewModel.stopTokenWait();
        if (mCancelCallback != null)
            mCancelCallback.cancel();

        super.onDismiss(dialog);
    }

    private void onCommand(Command command) {
        switch (command) {
            case SHOW_PROGRESS:
                findViewById(R.id.nfcCancelButton).setVisibility(View.INVISIBLE);
                findViewById(R.id.nfcSignImage).setVisibility(View.INVISIBLE);
                this.<TextView>findViewById(R.id.nfcMessage).setText(R.string.hold_nfc_card);
                findViewById(R.id.nfcProgressBar).setVisibility(View.VISIBLE);
                break;

            case DISMISS:
                dismiss();
                break;
        }
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return Objects.requireNonNull(getView()).findViewById(id);
    }

    @FunctionalInterface
    public interface CancelCallback {
        void cancel();
    }

    public class Control {
        private final FragmentManager mFragmentManager;

        public Control(FragmentManager fragmentManager) {
            mFragmentManager = fragmentManager;
        }

        public void show(@Nullable CancelCallback cancelCallback) {
            if (cancelCallback != null)
                mCancelCallback = cancelCallback;

            NfcDetectCardFragment.this.show(mFragmentManager, "nfcDetectCard");
        }

        public void dismiss() {
            mCancelCallback = null;
            Objects.requireNonNull(mViewModel).setCommand(Command.DISMISS);
        }
    }
}
