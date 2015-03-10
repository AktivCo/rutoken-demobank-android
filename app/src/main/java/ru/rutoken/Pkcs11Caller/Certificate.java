
package ru.rutoken.Pkcs11Caller;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;

import org.spongycastle.asn1.x500.X500Name;

import ru.rutoken.Pkcs11.CK_ATTRIBUTE;
import ru.rutoken.Pkcs11.Pkcs11Constants;
import ru.rutoken.Pkcs11.RtPkcs11;
import ru.rutoken.Pkcs11Caller.exception.CertNotFoundException;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;

public class Certificate {
    private X500Name mSubject;
    private byte[] mId;

    public Certificate(RtPkcs11 pkcs11, NativeLong session, NativeLong object)
            throws Pkcs11CallerException {
        CK_ATTRIBUTE[] attributes = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);
        attributes[0].type = Pkcs11Constants.CKA_SUBJECT;
        attributes[1].type = Pkcs11Constants.CKA_ID;

        NativeLong rv = pkcs11.C_GetAttributeValue(session, object,
                attributes, new NativeLong(attributes.length));
        if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

        for (CK_ATTRIBUTE attr : attributes) {
            attr.pValue = new Memory(attr.ulValueLen.intValue());
        }

        rv = pkcs11.C_GetAttributeValue(session, object,
                attributes, new NativeLong(attributes.length));
        if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

        byte[] subjectValue =
                attributes[0].pValue.getByteArray(0, attributes[0].ulValueLen.intValue());
        mSubject = X500Name.getInstance(subjectValue);
        if (mSubject == null) throw new CertNotFoundException();

        mId = attributes[1].pValue.getByteArray(0, attributes[1].ulValueLen.intValue());
    }

    public X500Name getSubject() {
        return mSubject;
    }

    public byte[] getId() {
        return mId;
    }
}
