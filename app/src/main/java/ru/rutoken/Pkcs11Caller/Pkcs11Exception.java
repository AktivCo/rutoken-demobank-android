package ru.rutoken.Pkcs11Caller;

import ru.rutoken.Pkcs11.Pkcs11Constants;

/**
 * Created by mironenko on 07.08.2014.
 */
public class Pkcs11Exception extends Exception {
    private int code;

    private Pkcs11Exception(int code) {
        super(getLocalizedDescription(code));
    }

    private static String getLocalizedDescription(int code) {
        switch (code) {
            case Pkcs11Constants.CKR_PIN_INCORRECT:
                return "PIN incorrect";
            case Pkcs11Constants.CKR_PIN_INVALID:
                return "PIN invalid";
            case Pkcs11Constants.CKR_PIN_LEN_RANGE:
                return "Wrong PIN length";
            case Pkcs11Constants.CKR_PIN_LOCKED:
                return "PIN locked";
            case Pkcs11Constants.CKR_USER_PIN_NOT_INITIALIZED:
                return "PIN not initialized";
            default:
                return String.format("Unknown PKCS11 error %08x", code);
        }
    }

    public int getErrorCode() {return code;}

    public String localizedDescription() {
        return getLocalizedDescription(code);
    }

    public static Pkcs11Exception exceptionWithCode(int code) {
        return new Pkcs11Exception(code);
    }
}
