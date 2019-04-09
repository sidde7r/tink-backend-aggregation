package se.tink.backend.aggregation.nxgen.http.truststrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.conn.ssl.TrustStrategy;

// good site to test this: https://badssl.com/
public class TrustAllCertificatesStrategy implements TrustStrategy {
    @Override
    public boolean isTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        return true;
    }
}
