package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class SabadellCryptoUtils {

    public static String getEncryptedParamAsB64String(String param) {
        byte[] paramData = param.getBytes();
        byte[] encryptedData = encryptData(paramData);
        return EncodingUtils.encodeAsBase64String(encryptedData);
    }

    private static byte[] encryptData(byte[] input) {
        RSAPublicKey publicKey = getPublicKeyFromCertificate();
        return RSA.encryptEcbPkcs1(publicKey, input);
    }

    private static RSAPublicKey getPublicKeyFromCertificate() {
        byte[] certBytes = EncodingUtils.decodeBase64String(SabadellConstants.Authentication.CERTIFICATE_B64);
        ByteArrayInputStream inputStream  =  new ByteArrayInputStream(certBytes);

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(inputStream);
            return (RSAPublicKey) cert.getPublicKey();
        } catch (CertificateException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
