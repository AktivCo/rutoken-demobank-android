package ru.rutoken.demobank.ui.nfc;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ru.rutoken.demobank.R;

public class NfcDetectCardFragment extends BottomSheetDialogFragment {
    private static final String CONTROL_ID_ARGUMENT = "controlId";
    private NfcDetectCardViewModel mViewModel;

    static NfcDetectCardFragment newInstance(int controlId) {
        final NfcDetectCardFragment fragment = new NfcDetectCardFragment();
        final Bundle arguments = new Bundle();
        arguments.putInt(CONTROL_ID_ARGUMENT, controlId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false);
        return inflater.inflate(R.layout.nfc_detect_card_fragment, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        findViewById(R.id.nfcCancelButton).setOnClickListener((View v) -> requireDialog().cancel());

        mViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                final Bundle arguments = Objects.requireNonNull(getArguments());
                if (!arguments.containsKey(CONTROL_ID_ARGUMENT))
                    throw new IllegalArgumentException(CONTROL_ID_ARGUMENT + " is not specified");
                return Objects.requireNonNull(modelClass.cast(new NfcDetectCardViewModel(
                        arguments.getInt(CONTROL_ID_ARGUMENT))));
            }
        }).get(NfcDetectCardViewModel.class);

        mViewModel.getWorkProgressFlag().observe(getViewLifecycleOwner(), this::onWorkProgressChanged);
        mViewModel.getDismissAction().observe(getViewLifecycleOwner(), value -> dismiss());
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        mViewModel.cancel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO Find a proper way to set animation
        Objects.requireNonNull(Objects.requireNonNull(getDialog())
                .getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    private void onWorkProgressChanged(boolean progress) {
        findViewById(R.id.nfcCancelButton).setVisibility(progress ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.nfcSignImage).setVisibility(progress ? View.INVISIBLE : View.VISIBLE);
        this.<TextView>findViewById(R.id.nfcMessage)
                .setText(progress ? R.string.hold_nfc_card : R.string.attach_nfc_card);
        findViewById(R.id.nfcProgressBar).setVisibility(progress ? View.VISIBLE : View.INVISIBLE);
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return Objects.requireNonNull(getView()).findViewById(id);
    }
}
