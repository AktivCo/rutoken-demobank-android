/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.utils;

import ru.rutoken.demobank.R;

public class TokenBatteryCharge {
    static final int PERCENTS_FULL = 100;
    private static final int[] mBatteryVoltage = new int[PERCENTS_FULL + 1];
    static {
        fillBatteryPercentageArray();
    }

    private static void fillBatteryPercentageArray() {
        for (int i = 0; i < mBatteryVoltage.length; i++) {
            mBatteryVoltage[i] = 3500 + 7 * i;
        }
    }

    private static int getBatteryImageForPercent(int percent) {
        if (percent <= 15) {
            return R.drawable.battery_empty;
        } else if (percent >= 100) {
            return R.drawable.battery_charge;
        } else if (percent < 25) {
            return R.drawable.battery_1_sec;
        } else if (percent <= 50) {
            return R.drawable.battery_2_sec;
        } else if (percent <= 75) {
            return R.drawable.battery_3_sec;
        } else {
            return R.drawable.battery_4_sec;
        }
    }

    public static int getBatteryPercentage(int batteryVoltage) {
        int i = 0;
        for (i = 0; i < mBatteryVoltage.length - 1; i++) {
            if ((batteryVoltage < mBatteryVoltage[i])
                    || ((batteryVoltage >= mBatteryVoltage[i]) && (batteryVoltage <= mBatteryVoltage[i + 1]))) {
                break;
            }
        }
        return i;
    }

    public static int getBatteryImageForVoltage(int batteryVoltage) {
        return getBatteryImageForPercent(getBatteryPercentage(batteryVoltage));
    }
}
