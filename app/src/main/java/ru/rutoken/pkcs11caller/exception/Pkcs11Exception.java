/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

import com.sun.jna.NativeLong;

import ru.rutoken.pkcs11jna.Pkcs11Constants;

public class Pkcs11Exception extends Pkcs11CallerException {
    private final NativeLong mCode;

    private Pkcs11Exception(NativeLong code) {
        super(getLocalizedDescription(code));
        mCode = code;
    }

    private static String getLocalizedDescription(NativeLong code) {
        if (code.longValue() == Pkcs11Constants.CKR_PIN_INCORRECT) return "PIN incorrect";
        else if (code.longValue() == Pkcs11Constants.CKR_PIN_INVALID) return "PIN invalid";
        else if (code.longValue() == Pkcs11Constants.CKR_PIN_LEN_RANGE) return "Wrong PIN length";
        else if (code.longValue() == Pkcs11Constants.CKR_PIN_LOCKED) return "PIN locked";
        else if (code.longValue() == Pkcs11Constants.CKR_USER_PIN_NOT_INITIALIZED) return "PIN not initialized";
        else return String.format("Unknown PKCS11 error %08x", code.intValue());
    }

    public NativeLong getErrorCode() {
        return mCode;
    }

    public static void throwIfNotOk(NativeLong code) throws Pkcs11Exception {
        if (code.longValue() != Pkcs11Constants.CKR_OK)
            throw new Pkcs11Exception(code);
    }
}
