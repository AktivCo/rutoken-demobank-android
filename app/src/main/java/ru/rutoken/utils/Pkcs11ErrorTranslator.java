package ru.rutoken.utils;

import android.content.Context;
import android.content.res.Resources;

import com.sun.jna.NativeLong;

import java.util.HashMap;
import java.util.Map;

import ru.rutoken.demobank.R;

/**
 * Created by mironenko on 02.09.2014.
 */
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
        if(null != mContext)
            return;
        mContext = context;
        Resources res = mContext.getResources();
        if(null == res) {
            return;
        }
        int intRV[] = res.getIntArray(R.array.rv);
        String messages[] = res.getStringArray(R.array.rvMessages);
        assert(intRV.length == messages.length);
        for (int i=0; i<intRV.length ; ++i) {
            mErrorMessages.put(new NativeLong(intRV[i]), messages[i]);
        }
        mGenericMessage = res.getString(R.string.generic_error);
    }
    public String messageForRV(NativeLong rv) {
        String message = mErrorMessages.get(rv);
        if(null == message) {
            if(null != rv) {
                message = String.format(mGenericMessage, rv.longValue());
            } else {
                message = "Unknown PKCS#11 error";
            }
        }
        return message;
    }
}
