package se.tink.sa.framework.security.impl;

import com.google.common.io.Files;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalEidasServiceVerificationCertsLoaderImpl
        extends AbstractEidasServiceVerificationCertsLoader {

    private static final String AGG_CERT = "data/eidas_dev_certificates/aggregation-staging-ca.pem";

    @Override
    protected Certificate loadCertificate() throws IOException, CertificateException {
        Certificate certificate = null;

        byte[] pemData =
                Files.toByteArray(
                        new File("data/eidas_dev_certificates/aggregation-staging-ca.pem"));

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(pemData));

        return certificate;
    }

    @Override
    protected KeyStore loadClientKeyStore()
            throws NoSuchProviderException, KeyStoreException, IOException, CertificateException,
                    NoSuchAlgorithmException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        File clientCertificateFile = new File(System.getProperty("user.home"), "eidas_client.p12");
        KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
        keyStore.load(new FileInputStream(clientCertificateFile), "changeme".toCharArray());
        return keyStore;
    }
}
