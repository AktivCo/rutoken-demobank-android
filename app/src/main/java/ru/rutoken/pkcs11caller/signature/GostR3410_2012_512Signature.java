package ru.rutoken.pkcs11caller.signature;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

class GostR3410_2012_512Signature extends AbstractSignature {

    GostR3410_2012_512Signature(Pkcs11 pkcs11, long sessionHandle) {
        super(pkcs11, sessionHandle);
    }

    @Override
    public byte[] sign(byte[] data) throws Pkcs11Exception {
        return innerSign(makeMechanism(RtPkcs11Constants.CKM_GOSTR3410_WITH_GOSTR3411_12_512), data);
    }
}
