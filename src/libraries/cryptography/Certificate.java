package se.tink.libraries.cryptography;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public final class Certificate {

    public static String getX509SerialNumber(final String x509B64Pem) {
        final CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
        final byte[] x509Bytes = Base64.getDecoder().decode(x509B64Pem);
        final InputStream x509B64PemStream = new ByteArrayInputStream(x509Bytes);
        final X509Certificate x509;
        try {
            x509 = (X509Certificate) certFactory.generateCertificate(x509B64PemStream);
        } catch (CertificateException e) {
            throw new IllegalStateException(e);
        }
        return x509.getSerialNumber().toString();
    }
}
