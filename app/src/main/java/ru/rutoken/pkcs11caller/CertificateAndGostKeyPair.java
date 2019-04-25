package ru.rutoken.pkcs11caller;

import java.util.Objects;

public class CertificateAndGostKeyPair {
    private final Certificate mCertificate;
    private final GostKeyPair mKeyPair;
    CertificateAndGostKeyPair(Certificate certificate, GostKeyPair gostKeyPair) {
        mCertificate = Objects.requireNonNull(certificate);
        mKeyPair = Objects.requireNonNull(gostKeyPair);
    }

    public Certificate getCertificate() {
        return mCertificate;
    }

    public GostKeyPair getGostKeyPair() {
        return mKeyPair;
    }
}
