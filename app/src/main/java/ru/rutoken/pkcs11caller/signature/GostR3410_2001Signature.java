package ru.rutoken.pkcs11caller.signature;

import ru.rutoken.pkcs11caller.digest.Digest;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3410_2001Signature extends AbstractSignature {

    GostR3410_2001Signature(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] sign(final byte[] data) throws Pkcs11Exception {
        return innerSign(makeMechanism(RtPkcs11Constants.CKM_GOSTR3410), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3410_2001;
    }

    @Override
    public Digest.Type getDigestType() {
        return Digest.Type.GOSTR3411_1994;
    }
}
