package ru.rutoken.demobank.utils;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

import ru.rutoken.demobank.ui.pcscinstall.PcscInstallDialogFragment;

public final class PcscChecker {
    public final static String PCSC_PACKAGE_NAME = "ru.rutoken";

    private PcscChecker() {
    }

    public static void checkPcscInstallation(FragmentActivity activity) {
        if (!isRutokenPanelInstalled(activity)) {
            DialogFragment newFragment = new PcscInstallDialogFragment();
            newFragment.show(activity.getSupportFragmentManager(), "PcscInstallFragment");
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private static boolean isRutokenPanelInstalled(FragmentActivity activity) {
        PackageManager packageManager = activity.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(PCSC_PACKAGE_NAME,
                            PackageManager.PackageInfoFlags.of(0));
                } else {
                    packageManager.getPackageInfo(PCSC_PACKAGE_NAME, 0);
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            boolean pcscInstalled = false;
            List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);
            for (ApplicationInfo packageInfo : packages) {
                if (packageInfo.packageName.equals(PCSC_PACKAGE_NAME)) {
                    pcscInstalled = true;
                    break;
                }
            }

            return pcscInstalled;
        }
    }
}
