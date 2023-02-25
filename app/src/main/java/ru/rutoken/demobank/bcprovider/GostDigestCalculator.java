package ru.rutoken.demobank.bcprovider;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.io.DigestOutputStream;
import org.bouncycastle.operator.DigestCalculator;

import java.io.OutputStream;
import java.security.InvalidParameterException;

import ru.rutoken.demobank.bcprovider.digest.Digest;
import ru.rutoken.demobank.pkcs11caller.GostOids;

class GostDigestCalculator implements DigestCalculator {
    private final Digest mDigest;
    private final DigestOutputStream mDigestStream;

    GostDigestCalculator(Digest.Type digestType, long sessionHandle) {
        mDigest = Digest.getInstance(digestType, sessionHandle);
        mDigestStream = new DigestOutputStream(mDigest);
    }

    @Override
    public AlgorithmIdentifier getAlgorithmIdentifier() {
        Digest.Type digestType = mDigest.getType();
        if (digestType == Digest.Type.GOSTR3411_1994)
            return new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3411);
        else if (digestType == Digest.Type.GOSTR3411_2012_256)
            return new AlgorithmIdentifier(new ASN1ObjectIdentifier(GostOids.STRING_OID_3411_2012_256));
        else if (digestType == Digest.Type.GOSTR3411_2012_512)
            return new AlgorithmIdentifier(new ASN1ObjectIdentifier(GostOids.STRING_OID_3411_2012_512));
        throw new InvalidParameterException();
    }

    @Override
    public byte[] getDigest() {
        try {
            byte[] hash = new byte[mDigest.getDigestSize()];
            mDigest.doFinal(hash, 0);

            return hash;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return mDigestStream;
    }
}
