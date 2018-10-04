/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import java.io.UnsupportedEncodingException;

class Utils {
    static String removeTrailingSpaces(byte[] string) {
        String result = "";
        try {
            result = (new String(string, "UTF-8")).replaceAll(" *$", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
