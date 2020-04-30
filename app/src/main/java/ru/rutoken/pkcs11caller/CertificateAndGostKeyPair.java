package ru.rutoken.pkcs11caller;

import java.util.Objects;

class CertificateAndGostKeyPair {
    private final Certificate mCertificate;
    private final GostKeyPair mKeyPair;

    CertificateAndGostKeyPair(Certificate certificate, GostKeyPair gostKeyPair) {
        mCertificate = Objects.requireNonNull(certificate);
        mKeyPair = Objects.requireNonNull(gostKeyPair);
    }

    Certificate getCertificate() {
        return mCertificate;
    }

    GostKeyPair getGostKeyPair() {
        return mKeyPair;
    }
}
