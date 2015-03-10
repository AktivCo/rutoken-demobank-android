/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller.exception;

import com.sun.jna.NativeLong;

import ru.rutoken.Pkcs11.Pkcs11Constants;

public class Pkcs11Exception extends Pkcs11CallerException {
    private NativeLong code;

    private Pkcs11Exception(NativeLong rv) {
        super(getLocalizedDescription(rv));
        code = rv;
    }

    private static String getLocalizedDescription(NativeLong code) {
        if (code.equals(Pkcs11Constants.CKR_PIN_INCORRECT)) return "PIN incorrect";
        else if (code.equals(Pkcs11Constants.CKR_PIN_INVALID)) return "PIN invalid";
        else if (code.equals(Pkcs11Constants.CKR_PIN_LEN_RANGE)) return "Wrong PIN length";
        else if (code.equals(Pkcs11Constants.CKR_PIN_LOCKED)) return "PIN locked";
        else if (code.equals(Pkcs11Constants.CKR_USER_PIN_NOT_INITIALIZED)) return "PIN not initialized";
        else return String.format("Unknown PKCS11 error %08x", code.intValue());
    }

    public NativeLong getErrorCode() {
        return code;
    }

    public static Pkcs11Exception exceptionWithCode(NativeLong code) {
        return new Pkcs11Exception(code);
    }
}
