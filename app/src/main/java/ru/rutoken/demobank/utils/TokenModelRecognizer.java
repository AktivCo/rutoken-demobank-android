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

public class TokenModelRecognizer {
    private static volatile TokenModelRecognizer mInstance = null;
    private final Context mContext;
    private final Map<String, String> mModelNames = new HashMap<>();

    private TokenModelRecognizer(Context context) {
        mContext = context.getApplicationContext();
        fillInModelNames();
    }

    public static synchronized TokenModelRecognizer getInstance(Context context) {
        TokenModelRecognizer localInstance = mInstance;
        if (localInstance == null) {
            synchronized (TokenModelRecognizer.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new TokenModelRecognizer(context);
                }
            }
        }
        return localInstance;
    }

    private void fillInModelNames() {
        Resources res = mContext.getResources();
        if (res == null) {
            return;
        }
        String[] pkcs11Models = res.getStringArray(R.array.pkcs11_names);
        String[] marketingModels = res.getStringArray(R.array.marketing_names);
        Assert.assertTrue("incompatible length", pkcs11Models.length == marketingModels.length);
        for (int i = 0; i < pkcs11Models.length; ++i) {
            mModelNames.put(pkcs11Models[i], marketingModels[i]);
        }
    }

    public String marketingNameForPkcs11Name(String pkcs11Model) {
        String marketingModelName = mModelNames.get(pkcs11Model);
        if (marketingModelName == null) {
            marketingModelName = "Unsupported Model";
        }
        return marketingModelName;
    }
}
