package ru.rutoken.demobank.utils;

import android.content.pm.ApplicationInfo;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import ru.rutoken.demobank.ui.pcscinstall.PcscInstallDialogFragment;

public final class PcscChecker {
    public final static String PCSC_PACKAGE_NAME = "ru.rutoken";

    private PcscChecker() {
    }

    public static void checkPcscInstallation(FragmentActivity activity) {
        List<ApplicationInfo> packages = activity.getPackageManager().getInstalledApplications(0);
        boolean pcscInstalled = false;
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(PCSC_PACKAGE_NAME)) {
                pcscInstalled = true;
                break;
            }
        }

        if (!pcscInstalled) {
            DialogFragment newFragment = new PcscInstallDialogFragment();
            newFragment.show(activity.getSupportFragmentManager(), "PcscInstallFragment");
        }
    }
}
