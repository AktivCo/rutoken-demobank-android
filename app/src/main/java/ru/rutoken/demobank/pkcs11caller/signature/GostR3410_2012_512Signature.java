package ru.rutoken.demobank.pkcs11caller.signature;

import ru.rutoken.demobank.pkcs11caller.digest.Digest;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3410_2012_512Signature extends AbstractSignature {

    GostR3410_2012_512Signature(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] sign(final byte[] data) throws Pkcs11Exception {
        return innerSign(makeMechanism(RtPkcs11Constants.CKM_GOSTR3410_512), data);
    }

    @Override
    public Type getType() {
        return Type.GOSTR3410_2012_512;
    }

    @Override
    public Digest.Type getDigestType() {
        return Digest.Type.GOSTR3411_2012_512;
    }
}
