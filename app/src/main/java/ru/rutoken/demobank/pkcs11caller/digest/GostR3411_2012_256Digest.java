package ru.rutoken.demobank.pkcs11caller.digest;

import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3411_2012_256Digest extends AbstractDigest {
    private static final byte[] ATTR_GOSTR3411_2012_256 = {0x06, 0x08, 0x2a, (byte) 0x85, 0x03,
                                                           0x07, 0x01, 0x01, 0x02, 0x02};

    GostR3411_2012_256Digest(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] digest(final byte[] data) throws Pkcs11Exception {
        return innerDigest(makeMechanism(RtPkcs11Constants.CKM_GOSTR3411_12_256, ATTR_GOSTR3411_2012_256), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3411_2012_256;
    }
}
