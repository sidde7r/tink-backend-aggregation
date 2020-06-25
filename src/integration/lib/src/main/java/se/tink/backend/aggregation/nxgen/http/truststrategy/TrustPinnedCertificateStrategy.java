package se.tink.backend.aggregation.nxgen.http.truststrategy;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.TrustStrategy;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

/**
 * This trust strategy can be used when a bank is presenting a specific certificate that is invalid.
 * It will consider the connection trusted if the knwon certificate is presented, but fall back to
 * the default trust manager if not.
 */
public class TrustPinnedCertificateStrategy implements TrustStrategy, VerifyHostname {

    private Certificate pinnedCertificate;
    private X509TrustManager javaDefaultTrustManager;

    private TrustPinnedCertificateStrategy(
            Certificate pinnedCertificate, X509TrustManager javaDefaultTrustManager) {
        this.pinnedCertificate = pinnedCertificate;
        this.javaDefaultTrustManager = javaDefaultTrustManager;
    }

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain[0].equals(pinnedCertificate)) {
            return true;
        } else {
            try {
                javaDefaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                return false;
            }
            return true;
        }
    }

    public static TrustStrategy forCertificate(String pemBytes) {
        try {
            Certificate pinnedCertificate =
                    Pem.parseCertificate(pemBytes.getBytes(StandardCharsets.US_ASCII));

            final TrustManagerFactory javaDefaultTrustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            javaDefaultTrustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = javaDefaultTrustManagerFactory.getTrustManagers();
            X509TrustManager javaDefaultTrustManager = (X509TrustManager) trustManagers[0];
            return new TrustPinnedCertificateStrategy(pinnedCertificate, javaDefaultTrustManager);
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
