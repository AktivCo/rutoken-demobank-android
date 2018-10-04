/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;
import ru.rutoken.pkcs11caller.exception.CertNotFoundException;
import ru.rutoken.pkcs11caller.exception.CertParsingException;
import ru.rutoken.pkcs11caller.exception.KeyNotFoundException;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

public class Certificate {
    private X500Name mSubject;
    private byte[] mKeyPairId;

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

    public Certificate(RtPkcs11 pkcs11, NativeLong session, NativeLong object)
            throws Pkcs11CallerException {
        CK_ATTRIBUTE[] attributes = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);
        attributes[0].type = new NativeLong(Pkcs11Constants.CKA_SUBJECT);
        attributes[1].type = new NativeLong(Pkcs11Constants.CKA_VALUE);

        NativeLong rv = pkcs11.C_GetAttributeValue(session, object,
                attributes, new NativeLong(attributes.length));
        Pkcs11Exception.throwIfNotOk(rv);

        for (CK_ATTRIBUTE attr : attributes) {
            attr.pValue = new Memory(attr.ulValueLen.intValue());

        }

        rv = pkcs11.C_GetAttributeValue(session, object,
                attributes, new NativeLong(attributes.length));
        Pkcs11Exception.throwIfNotOk(rv);

        byte[] subjectValue =
                attributes[0].pValue.getByteArray(0, attributes[0].ulValueLen.intValue());
        mSubject = X500Name.getInstance(subjectValue);
        if (mSubject == null) throw new CertNotFoundException();

        byte[] keyValue;
        try {
            X509CertificateHolder certificateHolder = new X509CertificateHolder(
                    attributes[1].pValue.getByteArray(0, attributes[1].ulValueLen.intValue()));
            SubjectPublicKeyInfo publicKeyInfo = certificateHolder.getSubjectPublicKeyInfo();
            keyValue = publicKeyInfo.parsePublicKey().getEncoded();
        } catch (IOException exception) {
            throw new CertParsingException();
        }

        if (keyValue == null) throw new KeyNotFoundException();

        // уберём заголовок ключа (первые 2 байта)
        keyValue = Arrays.copyOfRange(keyValue, 2, keyValue.length);
        CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        final NativeLongByReference keyClass =
                new NativeLongByReference(new NativeLong(Pkcs11Constants.CKO_PUBLIC_KEY));
        template[0].type = new NativeLong(Pkcs11Constants.CKA_CLASS);
        template[0].pValue = keyClass.getPointer();
        template[0].ulValueLen = new NativeLong(NativeLong.SIZE);

        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(keyValue.length);
        valueBuffer.put(keyValue);
        template[1].type = new NativeLong(Pkcs11Constants.CKA_VALUE);
        template[1].pValue = Native.getDirectBufferPointer(valueBuffer);
        template[1].ulValueLen = new NativeLong(keyValue.length);

        NativeLong pubKeyHandle = findObject(pkcs11, session, template);
        if (pubKeyHandle == null) throw new KeyNotFoundException();

        CK_ATTRIBUTE[] idTemplate = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(1);
        idTemplate[0].type = new NativeLong(Pkcs11Constants.CKA_ID);

        rv = pkcs11.C_GetAttributeValue(session, pubKeyHandle,
                idTemplate, new NativeLong(idTemplate.length));
        Pkcs11Exception.throwIfNotOk(rv);

        idTemplate[0].pValue = new Memory(idTemplate[0].ulValueLen.longValue());

        rv = pkcs11.C_GetAttributeValue(session, pubKeyHandle,
                idTemplate, new NativeLong(idTemplate.length));
        Pkcs11Exception.throwIfNotOk(rv);

        mKeyPairId = idTemplate[0].pValue.getByteArray(0, idTemplate[0].ulValueLen.intValue());
    }

    public X500Name getSubject() {
        return mSubject;
    }

    public NativeLong getPrivateKeyHandle(RtPkcs11 pkcs11, NativeLong session)
            throws Pkcs11CallerException {
        CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        final NativeLongByReference keyClass =
                new NativeLongByReference(new NativeLong(Pkcs11Constants.CKO_PRIVATE_KEY));
        template[0].type = new NativeLong(Pkcs11Constants.CKA_CLASS);
        template[0].pValue = keyClass.getPointer();
        template[0].ulValueLen = new NativeLong(NativeLong.SIZE);

        ByteBuffer idBuffer = ByteBuffer.allocateDirect(mKeyPairId.length);
        idBuffer.put(mKeyPairId);
        template[1].type = new NativeLong(Pkcs11Constants.CKA_ID);
        template[1].pValue = Native.getDirectBufferPointer(idBuffer);
        template[1].ulValueLen = new NativeLong(mKeyPairId.length);

        return findObject(pkcs11, session, template);
    }

    private NativeLong findObject(RtPkcs11 pkcs11, NativeLong session, CK_ATTRIBUTE[] template)
        throws Pkcs11CallerException {
        NativeLong rv = pkcs11.C_FindObjectsInit(session,
                template, new NativeLong(template.length));
        Pkcs11Exception.throwIfNotOk(rv);

        NativeLong objects[] = new NativeLong[1];
        NativeLongByReference count =
                new NativeLongByReference(new NativeLong(objects.length));
        rv = pkcs11.C_FindObjects(session, objects, new NativeLong(objects.length),
                count);

        NativeLong rv2 = pkcs11.C_FindObjectsFinal(session);
        Pkcs11Exception.throwIfNotOk(rv);
        Pkcs11Exception.throwIfNotOk(rv2);
        if (count.getValue().longValue() <= 0) return null;

        return objects[0];
    }
}
