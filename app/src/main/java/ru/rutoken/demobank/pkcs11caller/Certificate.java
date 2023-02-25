/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.pkcs11caller;

import android.util.Base64;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.rutoken.demobank.pkcs11caller.exception.CertParsingException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;

public class Certificate {
    private final X509CertificateHolder mCertificateHolder;
    private String mFingerprint;

    public Certificate(RtPkcs11 pkcs11, long session, long object)
            throws Pkcs11CallerException {
        CK_ATTRIBUTE[] attributes = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);
        attributes[0].type = new NativeLong(Pkcs11Constants.CKA_SUBJECT);
        attributes[1].type = new NativeLong(Pkcs11Constants.CKA_VALUE);

        NativeLong rv = pkcs11.C_GetAttributeValue(new NativeLong(session), new NativeLong(object),
                attributes, new NativeLong(attributes.length));
        Pkcs11Exception.throwIfNotOk(rv);

        for (CK_ATTRIBUTE attr : attributes) {
            attr.pValue = new Memory(attr.ulValueLen.intValue());
        }

        rv = pkcs11.C_GetAttributeValue(new NativeLong(session), new NativeLong(object),
                attributes, new NativeLong(attributes.length));
        Pkcs11Exception.throwIfNotOk(rv);

        try {
            byte[] der = attributes[1].pValue.getByteArray(0, attributes[1].ulValueLen.intValue());
            mCertificateHolder = new X509CertificateHolder(der);

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            mFingerprint = Base64.encodeToString(sha256.digest(der), Base64.NO_WRAP);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new CertParsingException();
        }
    }

    public X500Name getSubject() {
        return mCertificateHolder.getSubject();
    }

    X509CertificateHolder getCertificateHolder() {
        return mCertificateHolder;
    }

    public String fingerprint() {
        return mFingerprint;
    }

    public enum CertificateCategory {
        UNSPECIFIED(0),
        USER(1),
        AUTHORITY(2),
        OTHER(3);
        final int mValue;

        CertificateCategory(int value) {
            mValue = value;
        }

        int getValue() {
            return mValue;
        }
    }
}
