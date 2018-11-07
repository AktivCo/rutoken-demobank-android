/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.utils;

import ru.rutoken.demobank.R;

public class TokenBatteryCharge {
    private static final int MIN_VOLTAGE = 3500;
    private static final int VOLTAGE_STEP = 7;

    public static int getBatteryPercentage(int batteryVoltage) {
        return bound(0, (batteryVoltage - MIN_VOLTAGE) % VOLTAGE_STEP, 100);
    }

    public static int getBatteryImageForVoltage(int batteryVoltage) {
        return getBatteryImageForPercent(getBatteryPercentage(batteryVoltage));
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

    private static int bound(int min, int value, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
