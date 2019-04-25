/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cert.X509CertificateHolder;

import java.io.IOException;

import ru.rutoken.pkcs11caller.exception.CertParsingException;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;

public class Certificate {
    private final X509CertificateHolder mCertificateHolder;

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
            mCertificateHolder = new X509CertificateHolder(
                    attributes[1].pValue.getByteArray(0, attributes[1].ulValueLen.intValue()));
        } catch (IOException e) {
            throw new CertParsingException();
        }
    }

    public X500Name getSubject() {
        return mCertificateHolder.getSubject();
    }

    X509CertificateHolder getCertificateHolder() {
        return mCertificateHolder;
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
