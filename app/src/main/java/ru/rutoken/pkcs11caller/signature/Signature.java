package ru.rutoken.pkcs11caller.signature;

import java.security.InvalidParameterException;

import ru.rutoken.pkcs11caller.RtPkcs11Library;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

public interface Signature {
    enum Type {
        GOSTR3410_2001,
        GOSTR3410_2012_256,
        GOSTR3410_2012_512
    }

    static Signature getInstance(Type type, long sessionHandle) {
        switch (type) {
            case GOSTR3410_2001:
                return new GostR3410_2001Signature(RtPkcs11Library.getInstance(), sessionHandle);
            case GOSTR3410_2012_256:
                return new GostR3410_2012_256Signature(RtPkcs11Library.getInstance(), sessionHandle);
            case GOSTR3410_2012_512:
                return new GostR3410_2012_512Signature(RtPkcs11Library.getInstance(), sessionHandle);
            default:
                throw new InvalidParameterException();
        }
    }

    void signInit(long privateKeyHandle) throws Pkcs11Exception;

    byte[] sign(byte[] data) throws Pkcs11Exception;
}
