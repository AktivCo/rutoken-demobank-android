package ru.rutoken.bcprovider;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11caller.signature.Signature;

public class Pkcs7Signer {
    private final GostContentSigner mSigner;

    public Pkcs7Signer(Signature.Type signatureType, long sessionHandle) {
        mSigner = new GostContentSigner(signatureType, sessionHandle);
    }

    public byte[] sign(final byte[] data, long privateKeyHandle, final X509CertificateHolder certificate) throws Pkcs11Exception {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        try {
            generator.addCertificate(certificate);
            mSigner.signInit(privateKeyHandle);
            generator.addSignerInfoGenerator(new SignerInfoGeneratorBuilder(mSigner.getDigestProvider()).build(mSigner, certificate));
            CMSTypedData cmsData = new CMSProcessableByteArray(data);
            return generator.generate(cmsData, true).getEncoded();
        } catch (Pkcs11Exception e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
