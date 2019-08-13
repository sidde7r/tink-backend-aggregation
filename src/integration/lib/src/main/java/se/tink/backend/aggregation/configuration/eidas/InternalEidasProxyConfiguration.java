package se.tink.backend.aggregation.configuration.eidas;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

public class InternalEidasProxyConfiguration {
    private static final String DUMMY_CERT_NAME = "trustedcert";
    private static final String DUMMY_PASSWORD = "password";
    private final String host;
    private final String caPath;
    private final String tlsCrtPath;
    private final String tlsKeyPath;
    private final boolean localEidasDev;

    public InternalEidasProxyConfiguration(
            String host,
            String caPath,
            String tlsCrtPath,
            String tlsKeyPath,
            boolean localEidasDev) {
        this.host = host;
        this.caPath = caPath;
        this.tlsCrtPath = tlsCrtPath;
        this.tlsKeyPath = tlsKeyPath;
        this.localEidasDev = localEidasDev;
    }

    public String getHost() {
        return host;
    }

    public KeyStore getRootCaTrustStore()
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {

        final Certificate certificate;
        if (caPath != null) {
            certificate = Pem.parseCertificate(Files.toByteArray(new File(caPath)));

        } else if (localEidasDev) {
            // Running in local development, we can trust aggregation staging
            certificate =
                    Pem.parseCertificate(
                            Files.toByteArray(
                                    new File(
                                            "data/eidas_dev_certificates/aggregation-staging-ca.pem")));
        } else {
            throw new IllegalStateException("Trusted CA for eiDAS proxy not configured");
        }
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, DUMMY_PASSWORD.toCharArray());

        keyStore.setCertificateEntry(DUMMY_CERT_NAME, certificate);
        return keyStore;
    }

    public KeyStore getClientCertKeystore()
            throws KeyStoreException, IOException, NoSuchProviderException, CertificateException,
                    NoSuchAlgorithmException {
        if (tlsCrtPath != null && tlsKeyPath != null) {
            Certificate certificate = Pem.parseCertificate(Files.toByteArray(new File(tlsCrtPath)));
            PrivateKey privateKey = Pem.parsePrivateKey(Files.toByteArray(new File(tlsKeyPath)));

            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, "password".toCharArray());
            Certificate[] certificateChain = new Certificate[1];
            certificateChain[0] = certificate;

            keyStore.setKeyEntry("clientcert", privateKey, null, certificateChain);
            return keyStore;
        } else if (localEidasDev) {
            File clientCertificateFile =
                    new File(System.getProperty("user.home"), "eidas_client.p12");
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(new FileInputStream(clientCertificateFile), "changeme".toCharArray());
            return keyStore;

        } else {
            throw new IllegalStateException("Client certificate for eIDAS proxy not configured");
        }
    }
}
