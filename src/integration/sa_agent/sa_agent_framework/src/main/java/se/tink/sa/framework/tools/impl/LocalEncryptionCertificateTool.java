package se.tink.sa.framework.tools.impl;

import java.security.*;
import java.security.cert.X509Certificate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import se.tink.sa.framework.tools.EncryptionCertificateTool;

// TODO: This class is written only for test purposes and should not be included in production code,
// should be replaced with EidasEncryptionService as well as LocalCertEncryptionServiceFactory
@Slf4j
public class LocalEncryptionCertificateTool implements EncryptionCertificateTool {

    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    public LocalEncryptionCertificateTool(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
    }

    public byte[] toSHA256withRSA(String input) {
        byte[] result = null;
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);

            signer.update(input.getBytes());
            result = signer.sign();
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    public String getCertificateSerialNumber() {
        return certificate.getSerialNumber().toString();
    }

    public String getCertificate() {
        return Base64.encodeBase64String(certificate.toString().getBytes());
    }
}
