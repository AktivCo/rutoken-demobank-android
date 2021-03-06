package ru.rutoken.demobank.ui.pcscinstall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.utils.PcscChecker;

public class PcscInstallDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setNegativeButton(R.string.exit, (dialog, id) -> getActivity().finish());

        builder.setMessage(R.string.no_pcsc_message)
                .setTitle(R.string.no_pcsc_title);

        builder.setPositiveButton(R.string.install_control_panel, (dialog, id) -> {
            Activity activity = requireActivity();
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + PcscChecker.PCSC_PACKAGE_NAME)));
            } catch (ActivityNotFoundException e) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + PcscChecker.PCSC_PACKAGE_NAME)));
            }
            activity.finish();
        });

        return builder.create();
    }
}
