/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;

import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import ru.rutoken.pkcs11caller.signature.Signature;
import ru.rutoken.pkcs11caller.exception.KeyTypeNotSupported;
import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;
import ru.rutoken.pkcs11caller.exception.CertNotFoundException;
import ru.rutoken.pkcs11caller.exception.CertParsingException;
import ru.rutoken.pkcs11caller.exception.KeyNotFoundException;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

import static ru.rutoken.pkcs11jna.RtPkcs11Constants.CKA_GOSTR3411_PARAMS;

public class Certificate {
    private final Signature.Type mKeyType;
    private X500Name mSubject;
    private byte[] mKeyPairId;

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

        // уберём заголовок ключа (см ASN.1 Basic Encoding Rules)
        int pos = 2;
        if ((keyValue[1] & (byte)(1 << 7)) != 0)
            pos += keyValue[1] & (byte)(0xFF >> 1);
        keyValue = Arrays.copyOfRange(keyValue, pos, keyValue.length);

        CK_ATTRIBUTE[] pubKeyTemplate = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        final NativeLongByReference keyClass =
                new NativeLongByReference(new NativeLong(Pkcs11Constants.CKO_PUBLIC_KEY));
        pubKeyTemplate[0].type = new NativeLong(Pkcs11Constants.CKA_CLASS);
        pubKeyTemplate[0].pValue = keyClass.getPointer();
        pubKeyTemplate[0].ulValueLen = new NativeLong(NativeLong.SIZE);

        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(keyValue.length);
        valueBuffer.put(keyValue);
        pubKeyTemplate[1].type = new NativeLong(Pkcs11Constants.CKA_VALUE);
        pubKeyTemplate[1].pValue = Native.getDirectBufferPointer(valueBuffer);
        pubKeyTemplate[1].ulValueLen = new NativeLong(keyValue.length);

        NativeLong pubKeyHandle = findObject(pkcs11, session, pubKeyTemplate);

        if (pubKeyHandle == null) throw new KeyNotFoundException();

        mKeyType = getKeyType(session, pubKeyHandle, pkcs11);

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

    Signature.Type getKeyType() {
        return mKeyType;
    }

    NativeLong getPrivateKeyHandle(RtPkcs11 pkcs11, NativeLong session)
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

    private Signature.Type getKeyType(NativeLong session, NativeLong pubKeyHandle, RtPkcs11 pkcs11)
            throws Pkcs11Exception, KeyTypeNotSupported {
        CK_ATTRIBUTE[] pubKeyMechTemplate = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(1);

        pubKeyMechTemplate[0].type = new NativeLong(CKA_GOSTR3411_PARAMS);
        pubKeyMechTemplate[0].pValue = Pointer.NULL;
        pubKeyMechTemplate[0].ulValueLen = new NativeLong(0);

        NativeLong rv = pkcs11.C_GetAttributeValue(session, pubKeyHandle, pubKeyMechTemplate, new NativeLong(1));
        Pkcs11Exception.throwIfNotOk(rv);

        ByteBuffer mechTypeValueBuffer = ByteBuffer.allocateDirect(pubKeyMechTemplate[0].ulValueLen.intValue());
        pubKeyMechTemplate[0].pValue = Native.getDirectBufferPointer(mechTypeValueBuffer);

        rv = pkcs11.C_GetAttributeValue(session, pubKeyHandle, pubKeyMechTemplate, new NativeLong(1));
        Pkcs11Exception.throwIfNotOk(rv);

        byte[] parametersGostR3411 = pubKeyMechTemplate[0].pValue.getByteArray(0, pubKeyMechTemplate[0].ulValueLen.intValue() );

        if (Arrays.equals(parametersGostR3411, Digest.OID_3411_1994)) return Signature.Type.GOSTR3410_2001;
        else if (Arrays.equals(parametersGostR3411, Digest.OID_3411_2012_256)) return Signature.Type.GOSTR3410_2012_256;
        else if (Arrays.equals(parametersGostR3411, Digest.OID_3411_2012_512)) return Signature.Type.GOSTR3410_2012_512;
        else throw new KeyTypeNotSupported();
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
