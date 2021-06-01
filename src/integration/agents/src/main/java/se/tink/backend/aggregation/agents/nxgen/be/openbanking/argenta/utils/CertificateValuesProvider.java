package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import static java.util.Base64.getEncoder;

import com.google.common.base.Preconditions;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

public class CertificateValuesProvider {
    private static final int RADIX = 16;

    private CertificateValuesProvider() {}

    @SneakyThrows
    public static CertificateValues extractCertificateValues(String base64EncodedCertificates) {
        Preconditions.checkArgument(
                base64EncodedCertificates != null, "Must provide base64-encoded certificates");

        List<X509Certificate> certs =
                CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                        base64EncodedCertificates);

        return certs.stream()
                .findFirst()
                .map(CertificateValuesProvider::createCertificateValues)
                .orElseThrow(
                        () -> {
                            throw new RuntimeException("error");
                            // todo throw new CertificateException("error");
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
