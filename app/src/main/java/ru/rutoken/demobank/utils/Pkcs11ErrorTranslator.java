/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.demobank.R;

public class Pkcs11ErrorTranslator {
    private static Pkcs11ErrorTranslator INSTANCE;
    private final Map<Long, String> mErrorMessages = new HashMap<>();
    private String mGenericMessage;

    private Pkcs11ErrorTranslator(Context context) {
        Resources res = context.getApplicationContext().getResources();
        if (res == null) {
            return;
        }
        int[] intRV = res.getIntArray(R.array.rv);
        String[] messages = res.getStringArray(R.array.rvMessages);
        Assert.assertTrue("incompatible length", intRV.length == messages.length);
        for (int i = 0; i < intRV.length; ++i) {
            mErrorMessages.put((long) intRV[i], messages[i]);
        }
        mGenericMessage = res.getString(R.string.generic_error);
    }

    public static synchronized Pkcs11ErrorTranslator getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new Pkcs11ErrorTranslator(context);
        return INSTANCE;
    }

    public String messageForRV(long rv) {
        String message = mErrorMessages.get(rv);
        if (message == null)
            message = String.format(mGenericMessage, rv);
        return message;
    }
}
