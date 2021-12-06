package se.tink.libraries.cryptography.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class Pem {

    public static PrivateKey parsePrivateKey(byte[] pemData) throws IOException {
        PEMParser pemParser =
                new PEMParser(new InputStreamReader(new ByteArrayInputStream(pemData)));

        Object pemObject = pemParser.readObject();
        PrivateKeyInfo privateKeyInfo;
        if (pemObject instanceof PEMKeyPair) {
            privateKeyInfo = ((PEMKeyPair) pemObject).getPrivateKeyInfo();
        } else if (pemObject instanceof PrivateKeyInfo) {
            privateKeyInfo = (PrivateKeyInfo) pemObject;
        } else {
            throw new IllegalStateException("Private key not in expected format");
        }

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return converter.getPrivateKey(privateKeyInfo);
    }

    public static Certificate parseCertificate(byte[] pemData) throws CertificateException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return certificateFactory.generateCertificate(new ByteArrayInputStream(pemData));
    }
}
