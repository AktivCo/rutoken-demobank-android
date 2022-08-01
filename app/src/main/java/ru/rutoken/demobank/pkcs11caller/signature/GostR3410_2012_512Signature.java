package ru.rutoken.demobank.pkcs11caller.signature;

import static ru.rutoken.pkcs11jna.Pkcs11Tc26Constants.CKM_GOSTR3410_512;

import ru.rutoken.demobank.bcprovider.digest.Digest;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;

class GostR3410_2012_512Signature extends AbstractSignature {

    GostR3410_2012_512Signature(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] sign(final byte[] data) throws Pkcs11Exception {
        return innerSign(makeMechanism(CKM_GOSTR3410_512), data);
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
