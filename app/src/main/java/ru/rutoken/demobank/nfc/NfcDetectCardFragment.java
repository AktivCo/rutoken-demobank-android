package ru.rutoken.demobank.nfc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ru.rutoken.demobank.R;


public class NfcDetectCardFragment extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        return inflater.inflate(R.layout.nfc_detect_card_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TODO: Find a proper way to set animation
        Objects.requireNonNull(
                Objects.requireNonNull(getDialog())
                        .getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
    }
}
