package ru.rutoken.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.List;

public class PackageInfoHelper {
    static public boolean installed(final Context context, final String packageName) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appInfoList = pm.getInstalledApplications(pm.GET_META_DATA);
        boolean isAppInstalled = false;
        for (ApplicationInfo appInfo : appInfoList) {
            if (packageName.equals(appInfo.packageName)) {
                isAppInstalled = true;
                break;
            }
        }
        return isAppInstalled;
    }
}