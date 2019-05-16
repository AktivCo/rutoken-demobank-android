package ru.rutoken.bcprovider;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Objects;

import ru.rutoken.pkcs11caller.GostOids;
import ru.rutoken.pkcs11caller.digest.Digest;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

public class GostDigestCalculator implements DigestCalculator {
    private final Digest mDigest;
    private final ByteArrayOutputStream mStream = new ByteArrayOutputStream();

    GostDigestCalculator(Digest.Type digestType, long sessionHandle) {
        mDigest = Digest.getInstance(Objects.requireNonNull(digestType), sessionHandle);
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
        byte[] data = mStream.toByteArray();
        try {
            return mDigest.digest(data);
        } catch(Pkcs11Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return mStream;
    }
}
