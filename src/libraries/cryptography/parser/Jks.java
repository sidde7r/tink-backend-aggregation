package se.tink.libraries.cryptography.parser;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Jks {

    public static KeyStore getSystemTrustStoreWithAdditionalCertificates(
            List<Certificate> certificates) {
        try {
            KeyStore truststore = KeyStore.getInstance("JKS");
            truststore.load(null, "changeit".toCharArray());
            int index = 0;
            for (Certificate c : getSystemIssuers()) {
                truststore.setCertificateEntry("trustedca-" + (index++), c);
            }
            for (Certificate c : certificates) {
                truststore.setCertificateEntry("trustedca-" + (index++), c);
            }

            return truststore;
        } catch (IOException
                | CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException e) {
            throw new IllegalStateException("Couldn't initialise trust store", e);
        }
    }

    private static List<Certificate> getSystemIssuers()
            throws NoSuchAlgorithmException, KeyStoreException {

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1) {
            throw new IllegalStateException("expected exactly 1 system trust manager");
        }
        X509TrustManager trustManager =
                (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
        return Arrays.asList(trustManager.getAcceptedIssuers());
    }
}
