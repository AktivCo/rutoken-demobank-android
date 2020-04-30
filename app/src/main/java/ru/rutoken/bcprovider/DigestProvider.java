package ru.rutoken.bcprovider;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;

import java.util.Objects;

class DigestProvider implements DigestCalculatorProvider {
    private final DigestCalculator mDigestCalculator;

    DigestProvider(final DigestCalculator digestCalculator) {
        mDigestCalculator = Objects.requireNonNull(digestCalculator);
    }

    @Override
    public DigestCalculator get(AlgorithmIdentifier algorithmIdentifier) {
        // We don't check algorithmIdentifier because Bouncy Castle supports only
        // CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001 algorithm
        return mDigestCalculator;
    }
}
