package se.tink.libraries.jersey.utils;

import com.google.common.base.Preconditions;
import eu.geekplace.javapinning.pin.Pin;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.conn.ssl.TrustStrategy;

// Leaf certificate pinning. Code taken from JavaPinning.
class PinServerCertificateTrustStrategy implements TrustStrategy {

    private final Collection<Pin> pins;

    public PinServerCertificateTrustStrategy(Collection<Pin> pins) {
        this.pins = pins;
    }

    private static String encodeAsHexString(byte[] binaryData) {
        return Hex.encodeHexString(binaryData);
    }

    @Override
    public boolean isTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {
        Preconditions.checkArgument(x509Certificates.length > 0, "No server certificates.");
        final X509Certificate leafCertificate = x509Certificates[0];
        if (!isPinned(leafCertificate)) {
            // Throw a CertificateException with a meaningful message. Note that we
            // use CERTPLAIN, which tends to be long, so colons as separator are of
            // no use and most other software UIs show the "public key" without
            // colons (and using lowercase letters).
            final String pinHexString = encodeAsHexString(leafCertificate.getEncoded());
            throw new CertificateException(
                    "Certificate not pinned. Use 'CERTPLAIN:"
                            + pinHexString
                            + "' to pin this certificate. But only pin the certificate if you are sure this "
                            + "is the correct certificate!");
        }
        return true;
    }

    private boolean isPinned(X509Certificate x509certificate) throws CertificateEncodingException {
        for (Pin pin : pins) {
            if (pin.pinsCertificate(x509certificate)) {
                return true;
            }
        }
        return false;
    }
}
