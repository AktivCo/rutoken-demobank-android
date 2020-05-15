package ru.rutoken.demobank.bcprovider;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.io.DigestOutputStream;
import org.bouncycastle.operator.ContentSigner;

import java.io.OutputStream;
import java.security.InvalidParameterException;

import ru.rutoken.demobank.bcprovider.digest.Digest;
import ru.rutoken.demobank.pkcs11caller.GostOids;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.demobank.pkcs11caller.signature.Signature;

class GostContentSigner implements ContentSigner {
    private final Signature mSignature;
    private final Digest mDigest;
    private final DigestProvider mDigestProvider;
    private final DigestOutputStream mDigestStream;

    GostContentSigner(Signature.Type signatureType, long sessionHandle) {
        mSignature = Signature.getInstance(signatureType, sessionHandle);
        mDigest = Digest.getInstance(mSignature.getDigestType(), sessionHandle);
        mDigestProvider = new DigestProvider(new GostDigestCalculator(mSignature.getDigestType(), sessionHandle));
        mDigestStream = new DigestOutputStream(mDigest);
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        Signature.Type signType = mSignature.getType();
        if (signType == Signature.Type.GOSTR3410_2001 || signType == Signature.Type.GOSTR3410_2012_256)
            return new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3410_2001);
        else if (signType == Signature.Type.GOSTR3410_2012_512)
            return new AlgorithmIdentifier(new ASN1ObjectIdentifier(GostOids.STRING_OID_3410_2012_512));
        throw new InvalidParameterException();
    }

    @Override
    public OutputStream getOutputStream() {
        return mDigestStream;
    }

    @Override
    public byte[] getSignature() {
        try {
            byte[] hash = new byte[mDigest.getDigestSize()];
            mDigest.doFinal(hash, 0);

            return mSignature.sign(hash);
        } catch (Pkcs11Exception e) {
            throw new RuntimeException(e);
        }
    }

    DigestProvider getDigestProvider() {
        return mDigestProvider;
    }

    void signInit(long privateKeyHandle) {
        mSignature.signInit(privateKeyHandle);
    }
}
