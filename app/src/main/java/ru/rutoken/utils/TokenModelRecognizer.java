package ru.rutoken.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.demobank.R;

public class TokenModelRecognizer {
    private static TokenModelRecognizer mInstance = null;
    private Context mContext;
    private Map<String, String> mModelNames = new HashMap<String, String>();
    public synchronized void init(Context context) {
        if(mContext != null)
            return;
        mContext = context;
        fillInModelNames();
    }

    protected void fillInModelNames() {
        Resources res = mContext.getResources();
        if(res == null) {
            return;
        }
        String pkcs11Models[] = res.getStringArray(R.array.pkcs11_names);
        String marketingModels[] = res.getStringArray(R.array.marketing_names);
        assert(pkcs11Models.length == marketingModels.length);
        for (int i = 0; i < pkcs11Models.length; ++i) {
            mModelNames.put(pkcs11Models[i], marketingModels[i]);
        }
    }

    public static synchronized TokenModelRecognizer getInstance() {
        TokenModelRecognizer localInstance = mInstance;
        if (localInstance == null) {
            synchronized (TokenModelRecognizer.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new TokenModelRecognizer();
                }
            }
        }
        return localInstance;
    }

    public String marketingNameForPkcs11Name(String pkcs11Model) {
        String marketingModelName = mModelNames.get(pkcs11Model);
        if(marketingModelName == null) {
            marketingModelName = "Unsupported Model";
        }
        return marketingModelName;
    }
}
