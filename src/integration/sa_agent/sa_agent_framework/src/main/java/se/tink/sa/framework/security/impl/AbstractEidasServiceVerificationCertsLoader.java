package se.tink.sa.framework.security.impl;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import se.tink.sa.framework.common.exceptions.StandaloneAgentException;
import se.tink.sa.framework.security.EidasServiceVerificationCertsLoader;

public abstract class AbstractEidasServiceVerificationCertsLoader
        implements EidasServiceVerificationCertsLoader {

    private static final String DUMMY_CERT_NAME = "trustedcert";
    private static final String DUMMY_PASSWORD = "password";

    @Override
    public KeyStore getRootCaTrustStore() {
        KeyStore keyStore = null;
        try {
            Certificate certificate = loadCertificate();
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, DUMMY_PASSWORD.toCharArray());
            keyStore.setCertificateEntry(DUMMY_CERT_NAME, certificate);
        } catch (Exception ex) {
            throw new StandaloneAgentException(ex);
        }
        return keyStore;
    }

    protected abstract Certificate loadCertificate() throws IOException, CertificateException;

    @Override
    public KeyStore getClientCertKeystore() {
        KeyStore keyStore = null;
        try {
            keyStore = loadClientKeyStore();
        } catch (Exception ex) {
            throw new StandaloneAgentException(ex);
        }
        return keyStore;
    }

    protected abstract KeyStore loadClientKeyStore()
            throws IOException, NoSuchProviderException, KeyStoreException, CertificateException,
                    NoSuchAlgorithmException;
}
