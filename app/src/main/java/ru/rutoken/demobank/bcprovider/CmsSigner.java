package ru.rutoken.demobank.bcprovider;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import ru.rutoken.demobank.pkcs11caller.signature.Signature;

public class CmsSigner {
    private final GostContentSigner mSigner;
    private final ByteArrayOutputStream mOutStream = new ByteArrayOutputStream();

    public CmsSigner(Signature.Type signatureType, long sessionHandle) {
        mSigner = new GostContentSigner(signatureType, sessionHandle);
    }

    public OutputStream initSignature(long privateKeyHandle, final X509CertificateHolder certificate,
                                      boolean isAttached) {
        CMSSignedDataStreamGenerator generator = new CMSSignedDataStreamGenerator();
        try {
            generator.addCertificate(certificate);
            mSigner.signInit(privateKeyHandle);
            generator.addSignerInfoGenerator(new SignerInfoGeneratorBuilder(mSigner.getDigestProvider())
                    .build(mSigner, certificate));

            return generator.open(mOutStream, isAttached);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] finishSignature() {
        return mOutStream.toByteArray();
    }
}
