/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller;

import java.io.UnsupportedEncodingException;

public class Utils {
    public static String removeTrailingSpaces(byte[] string) {
        String result = "";
        try {
            result = (new String(string, "UTF-8")).replaceAll(" *$", "");
        } catch (UnsupportedEncodingException e) {
        }
        return result;
    }
}
