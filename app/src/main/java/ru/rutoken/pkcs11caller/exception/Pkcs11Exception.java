/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

import com.sun.jna.NativeLong;

import ru.rutoken.pkcs11jna.Pkcs11Constants;

public class Pkcs11Exception extends Pkcs11CallerException {
    private final long mCode;

    public Pkcs11Exception(long code) {
        super(getLocalizedDescription(code));
        mCode = code;
    }

    public Pkcs11Exception(NativeLong code) {
        super(getLocalizedDescription(code.longValue()));
        mCode = code.longValue();
    }

    private static String getLocalizedDescription(long code) {
        if (code == Pkcs11Constants.CKR_PIN_INCORRECT) return "PIN incorrect";
        else if (code == Pkcs11Constants.CKR_PIN_INVALID) return "PIN invalid";
        else if (code == Pkcs11Constants.CKR_PIN_LEN_RANGE) return "Wrong PIN length";
        else if (code == Pkcs11Constants.CKR_PIN_LOCKED) return "PIN locked";
        else if (code == Pkcs11Constants.CKR_USER_PIN_NOT_INITIALIZED) return "PIN not initialized";
        else return String.format("PKCS11 error, code: 0x%08x", code);
    }

    public long getErrorCode() {
        return mCode;
    }

    public static void throwIfNotOk(NativeLong code) throws Pkcs11Exception {
        if (code.longValue() != Pkcs11Constants.CKR_OK)
            throw new Pkcs11Exception(code);
    }
}
