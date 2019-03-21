package ru.rutoken.pkcs11caller.digest;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3411_1994Digest extends AbstractDigest {

    GostR3411_1994Digest(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] digest(final byte[] data) throws Pkcs11Exception {
        return innerDigest(makeMechanism(RtPkcs11Constants.CKM_GOSTR3411), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3411_1994;
    }
}
