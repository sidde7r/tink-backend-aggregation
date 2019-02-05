package se.tink.backend.aggregation.nxgen.http.truststrategy;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.TrustStrategy;

public class TrustRootCaStrategy implements TrustStrategy {
    private final X509TrustManager customCaTrustManager;
    private final X509TrustManager fallbackTrustManager;

    private TrustRootCaStrategy(X509TrustManager customCaTrustManager, X509TrustManager fallbackTrustManager) {
        this.customCaTrustManager = customCaTrustManager;
        this.fallbackTrustManager = fallbackTrustManager;
    }

    private TrustRootCaStrategy(X509TrustManager customCaTrustManager) {
        this(customCaTrustManager, null);
    }

    // Create a TrustStrategy that will both trust servers with a signed certificate from the CA in the keystore
    // and, if that fails, trust any server with a certificate trusted by Java.
    // This will most likely be the most common use case for us.
    public static TrustRootCaStrategy createWithFallbackTrust(KeyStore keyStore) {
        try {
            final TrustManagerFactory customCaTrustManager = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            customCaTrustManager.init(keyStore);

            final TrustManagerFactory javaDefaultTrustManager = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            javaDefaultTrustManager.init((KeyStore)null);

            return new TrustRootCaStrategy(
                    (X509TrustManager) customCaTrustManager.getTrustManagers()[0],
                    (X509TrustManager) javaDefaultTrustManager.getTrustManagers()[0]);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    // Create a TrustStrategy that only trusts servers with a signed certificate from the CA in the keystore.
    public static TrustRootCaStrategy createWithoutFallbackTrust(KeyStore keyStore) {
        try {
            final TrustManagerFactory customCaTrustManager = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            customCaTrustManager.init(keyStore);

            return new TrustRootCaStrategy((X509TrustManager) customCaTrustManager.getTrustManagers()[0]);
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isTrusted(X509TrustManager manager, X509Certificate[] chain, String authType) {
        try {
            manager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        return isTrusted(customCaTrustManager, chain, authType) ||
                (Objects.nonNull(fallbackTrustManager) && isTrusted(fallbackTrustManager, chain, authType));
    }
}
