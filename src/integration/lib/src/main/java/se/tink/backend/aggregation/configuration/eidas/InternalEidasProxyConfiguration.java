package se.tink.backend.aggregation.configuration.eidas;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;
import se.tink.backend.eidasdevissuer.client.EdiClient;

public class InternalEidasProxyConfiguration {
    private static final String DUMMY_CERT_NAME = "trustedcert";
    private static final String DUMMY_PASSWORD = "password";
    private final String host;
    private final String caPath;
    private final String tlsCrtPath;
    private final String tlsKeyPath;
    private final String environment;
    private final boolean localEidasDev;

    // For lazy one-time initialization
    private static Certificate localEidasDevCertificate = null;

    public InternalEidasProxyConfiguration(
            String host,
            String caPath,
            String tlsCrtPath,
            String tlsKeyPath,
            String environment,
            boolean localEidasDev) {
        this.host = host;
        this.caPath = caPath;
        this.tlsCrtPath = tlsCrtPath;
        this.tlsKeyPath = tlsKeyPath;
        this.environment = environment;
        this.localEidasDev = localEidasDev;
    }

    public String getHost() {
        return host;
    }

    public String getEnvironment() {
        return environment;
    }

    public KeyStore getRootCaTrustStore()
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {

        final Certificate certificate;
        if (caPath != null) {
            certificate = Pem.parseCertificate(Files.toByteArray(new File(caPath)));
        } else if (localEidasDev) {
            /* If running in local development mode, we want to load development certificate one
            time into the class. This is needed because loading the same certificate too many times
            causes error when running tests in parallel */
            if (this.localEidasDevCertificate == null) {
                try {
                    localEidasDevCertificate =
                            Pem.parseCertificate(
                                    Files.toByteArray(
                                            new File(
                                                    "data/eidas_dev_certificates/aggregation-staging-ca.pem")));
                } catch (CertificateException | IOException e) {
                    throw new IllegalStateException(
                            "Could not load eIDAS development certificate, please ensure that the file "
                                    + "have data/eidas_dev_certificates/aggregation-staging-ca.pem exists",
                            e);
                }
            }
            // Running in local development, we can trust aggregation staging
            certificate = localEidasDevCertificate;
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
            return EdiClient.requestOrGetDevCert();
        } else {
            throw new IllegalStateException("Client certificate for eIDAS proxy not configured");
        }
    }
}
