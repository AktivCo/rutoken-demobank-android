package ru.rutoken.utils;

import android.content.pm.ApplicationInfo;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public final class PcscChecker {
    public final static String PCSC_PACKAGE_NAME = "ru.rutoken";

    private PcscChecker() {}

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
