package ru.rutoken.demobank.bcprovider.digest;

import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3411_1994Digest extends Pkcs11Digest {
    private static final byte[] ATTR_GOSTR3411_1994 = {0x06, 0x07, 0x2a, (byte) 0x85, 0x03, 0x02, 0x02, 0x1e, 0x01};

    GostR3411_1994Digest(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle, RtPkcs11Constants.CKM_GOSTR3411, ATTR_GOSTR3411_1994);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3411_1994;
    }

    @Override
    public String getAlgorithmName() {
        return "PKCS11-GOSTR3411-1994";
    }

    @Override
    public int getDigestSize() {
        return 32;
    }
}
