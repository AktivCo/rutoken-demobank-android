package ru.rutoken.bcprovider;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.SignerInfoGeneratorBuilder;

import ru.rutoken.pkcs11caller.signature.Signature;

public class Pkcs7Signer {
    private final GostContentSigner mSigner;

    public Pkcs7Signer(Signature.Type signatureType, long sessionHandle) {
        mSigner = new GostContentSigner(signatureType, sessionHandle);
    }

    public byte[] sign(final byte[] data, long privateKeyHandle, final X509CertificateHolder certificate) {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        try {
            generator.addCertificate(certificate);
            mSigner.signInit(privateKeyHandle);
            generator.addSignerInfoGenerator(new SignerInfoGeneratorBuilder(mSigner.getDigestProvider()).build(mSigner, certificate));
            CMSTypedData cmsData = new CMSProcessableByteArray(data);
            return generator.generate(cmsData, true).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
