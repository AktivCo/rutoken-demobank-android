/*
 * Copyright (c) 2022, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.application;

import android.app.Application;

import ru.rutoken.rtpcsc.RtPcsc;

public class DemobankApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RtPcsc.setAppContext(this);
    }
}
