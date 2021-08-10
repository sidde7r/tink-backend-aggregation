package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static java.util.Base64.getEncoder;

import com.google.common.base.Preconditions;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaCertificateException;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

public class CertificateValuesProvider {
    private static final int RADIX = 16;
    public static final String NULL_OR_EMPTY_CERTIFICATES =
            "Must provide base64-encoded certificates";
    public static final String INVALID_CERTIFICATES =
            "Must provide valid base64-encoded certificates";

    private CertificateValuesProvider() {}

    @SneakyThrows
    public static CertificateValues extractCertificateValues(String base64EncodedCertificates) {
        Preconditions.checkArgument(
                StringUtils.isNotEmpty(base64EncodedCertificates), NULL_OR_EMPTY_CERTIFICATES);

        List<X509Certificate> certs =
                CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                        base64EncodedCertificates);

        return certs.stream()
                .findFirst()
                .map(CertificateValuesProvider::createCertificateValues)
                .orElseThrow(
                        () -> {
                            throw new ArgentaCertificateException(INVALID_CERTIFICATES);
                        });
    }

    @SneakyThrows
    private static CertificateValues createCertificateValues(X509Certificate certificate) {
        return CertificateValues.builder()
                .clientSigningCertificate(getEncoder().encodeToString(certificate.getEncoded()))
                .serialNumber(certificate.getSerialNumber().toString(RADIX))
                .certificateAuthority(
                        certificate.getIssuerX500Principal().getName(X500Principal.RFC1779))
                .build();
    }
}
