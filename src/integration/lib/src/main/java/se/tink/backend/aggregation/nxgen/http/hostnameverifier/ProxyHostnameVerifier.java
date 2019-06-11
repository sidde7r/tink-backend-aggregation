package se.tink.backend.aggregation.nxgen.http.hostnameverifier;

import javax.net.ssl.SSLException;
import org.apache.http.conn.ssl.AbstractVerifier;

/**
 * This hostname verifier can be used when working with a proxy that MITM's HTTPS traffic. It
 * verifies that the certificate presented by the proxy matches the hostname specified specified in
 * the constructor, and trusts that the proxy is properly validating the certificate.
 */
public class ProxyHostnameVerifier extends AbstractVerifier {

    private final String proxyHostname;

    public ProxyHostnameVerifier(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    @Override
    public void verify(String host, String[] certCns, String[] certAlt) throws SSLException {
        verify(proxyHostname, certCns, certAlt, true);
    }
}
