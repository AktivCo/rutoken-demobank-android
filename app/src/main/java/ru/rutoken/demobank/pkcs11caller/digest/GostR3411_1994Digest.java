package ru.rutoken.demobank.pkcs11caller.digest;

import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3411_1994Digest extends AbstractDigest {
    private static final byte[] ATTR_GOSTR3411_1994 = {0x06, 0x07, 0x2a, (byte) 0x85, 0x03, 0x02, 0x02, 0x1e, 0x01};

    GostR3411_1994Digest(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] digest(final byte[] data) throws Pkcs11Exception {
        return innerDigest(makeMechanism(RtPkcs11Constants.CKM_GOSTR3411, ATTR_GOSTR3411_1994), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3411_1994;
    }
}
