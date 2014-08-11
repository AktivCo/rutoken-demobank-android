package ru.rutoken.Pkcs11Caller;

import java.io.UnsupportedEncodingException;

/**
 * Created by mironenko on 11.08.2014.
 */
public class Utils {
    public static String removeTrailingSpaces(byte[] string) {
        String result = "";
        try {
            result = (new String(string,"UTF-8")).replaceAll(" *$", "");
        } catch (UnsupportedEncodingException e) {};
        return result;
    }
}
