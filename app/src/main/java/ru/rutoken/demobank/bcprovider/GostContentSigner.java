package ru.rutoken.demobank.bcprovider;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import ru.rutoken.demobank.pkcs11caller.GostOids;
import ru.rutoken.demobank.pkcs11caller.digest.Digest;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.demobank.pkcs11caller.signature.Signature;

class GostContentSigner implements ContentSigner {
    private final Signature mSignature;
    private final Digest mDigest;
    private final DigestProvider mDigestProvider;
    private final ByteArrayOutputStream mStream = new ByteArrayOutputStream();

    GostContentSigner(Signature.Type signatureType, long sessionHandle) {
        mSignature = Signature.getInstance(signatureType, sessionHandle);
        mDigest = Digest.getInstance(mSignature.getDigestType(), sessionHandle);
        mDigestProvider = new DigestProvider(new GostDigestCalculator(mSignature.getDigestType(), sessionHandle));
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
        return mStream;
    }

    @Override
    public byte[] getSignature() {
        byte[] data = mStream.toByteArray();
        try {
            return mSignature.sign(mDigest.digest(data));
        } catch(Pkcs11Exception e) {
            e.printStackTrace();
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
