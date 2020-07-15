package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.utils;

import com.google.common.base.Strings;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.parser.Pem;

public final class IngBaseUtils {

    private IngBaseUtils() {}

    public static String getCertificateSerial(String encodedCertificate) {
        if (Strings.isNullOrEmpty(encodedCertificate)) {
            throw new IllegalStateException(
                    "Provided certificate is null or empty, make sure secrets are uploaded.");
        }

        try {
            final X509Certificate certificate =
                    (X509Certificate)
                            Pem.parseCertificate(Base64.getDecoder().decode(encodedCertificate));
            return String.format("SN=%x", certificate.getSerialNumber());
        } catch (CertificateException | IllegalArgumentException e) {
            throw new IllegalStateException("Could not parse given data as x509 certificate.");
        }
    }
}
