package ru.rutoken.pkcs11caller.digest;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3411_2012_512Digest extends AbstractDigest {

    GostR3411_2012_512Digest(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] digest(final byte[] data) throws Pkcs11Exception {
        return innerDigest(makeMechanism(RtPkcs11Constants.CKM_GOSTR3411_12_512), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3411_2012_512;
    }
}
