package ru.rutoken.demobank.nfc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.Factory;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ru.rutoken.demobank.R;


public class NfcDetectCardFragment extends BottomSheetDialogFragment {
    private final String mTokenSerial;

    public NfcDetectCardFragment(String tokenSerial) {
        mTokenSerial = Objects.requireNonNull(tokenSerial);
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

        NfcDetectCardViewModel viewModel = new ViewModelProvider(this, new Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) (new NfcDetectCardViewModel(mTokenSerial));
            }
        }).get(NfcDetectCardViewModel.class);

        viewModel.getProgress().observe(getViewLifecycleOwner(), this::onProgressChanged);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //TODO: Find a proper way to set animation
        Objects.requireNonNull(
                Objects.requireNonNull(getDialog())
                        .getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private void onProgressChanged(Boolean value) {
        if (value) {
            findViewById(R.id.cancel_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        } else {
            dismiss();
        }
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return Objects.requireNonNull(getView()).findViewById(id);
    }
}
