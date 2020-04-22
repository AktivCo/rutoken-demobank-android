package ru.rutoken.demobank.nfc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.Factory;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ru.rutoken.demobank.R;

import static ru.rutoken.demobank.nfc.NfcDetectCardViewModel.Command;

public class NfcDetectCardFragment extends BottomSheetDialogFragment {
    private static final String TOKEN_SERIAL_KEY = "tokenSerialKey";

    private NfcDetectCardViewModel mViewModel;

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
        findViewById(R.id.cancel_button).setOnClickListener((View v) -> dismiss());
        String tokenSerial = Objects.requireNonNull(getArguments()).getString(TOKEN_SERIAL_KEY);

        mViewModel = new ViewModelProvider(this, new Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) (new NfcDetectCardViewModel(tokenSerial));
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

    private void onCommand(Command command) {
        switch (command) {
            case SHOW_PROGRESS:
                findViewById(R.id.cancel_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                break;

            case DISMISS:
                dismiss();
                break;
        }

    }

    private <T extends View> T findViewById(@IdRes int id) {
        return Objects.requireNonNull(getView()).findViewById(id);
    }

    public class Control {
        private FragmentManager mFragmentManager;

        public Control(FragmentManager fragmentManager) {
            mFragmentManager = fragmentManager;
        }

        public void show() {
            NfcDetectCardFragment.this.show(mFragmentManager, "nfcDetectCard");
        }

        public void dismiss() {
            Objects.requireNonNull(mViewModel).setCommand(Command.DISMISS);
        }
    }
}
