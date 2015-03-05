package ru.rutoken.utils;

import android.content.Context;
import android.content.res.Resources;

import com.sun.jna.NativeLong;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.demobank.R;

public class Pkcs11ErrorTranslator {
    private static Pkcs11ErrorTranslator mInstance = null;
    private Context mContext;
    private Map<NativeLong,String> mErrorMessages = new HashMap<NativeLong,String>();
    private String mGenericMessage;

    public static synchronized Pkcs11ErrorTranslator getInstance() {
        Pkcs11ErrorTranslator localInstance = mInstance;
        if (localInstance == null) {
            synchronized (Pkcs11ErrorTranslator.class) {
                localInstance = mInstance;
                if (localInstance == null) {
                    mInstance = localInstance = new Pkcs11ErrorTranslator();
                }
            }
        }
        return localInstance;
    }

    public synchronized void init(Context context) {
        if (mContext != null)
            return;
        mContext = context;
        Resources res = mContext.getResources();
        if (res == null) {
            return;
        }
        int intRV[] = res.getIntArray(R.array.rv);
        String messages[] = res.getStringArray(R.array.rvMessages);
        assert(intRV.length == messages.length);
        for (int i = 0; i < intRV.length; ++i) {
            mErrorMessages.put(new NativeLong(intRV[i]), messages[i]);
        }
        mGenericMessage = res.getString(R.string.generic_error);
    }
    
    public String messageForRV(NativeLong rv) {
        String message = mErrorMessages.get(rv);
        if (message == null) {
            if (rv != null) {
                message = String.format(mGenericMessage, rv.longValue());
            } else {
                message = "Unknown PKCS#11 error";
            }
        }
        return message;
    }
}
