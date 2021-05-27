package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

public class CertificateValuesProvider {
    @SneakyThrows
    public static CertificateValues getCertificateValues(String base64EncodedCertificates) {
        List<X509Certificate> certs =
                CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                        base64EncodedCertificates);
        X509Certificate cert = certs.get(0);

        return CertificateValues.builder()
                .clientSigningCertificate(Base64.getEncoder().encodeToString(cert.getEncoded()))
                .serialNumber(cert.getSerialNumber().toString(16))
                .certificateAuthority(cert.getIssuerX500Principal().getName(X500Principal.RFC1779))
                .build();
    }
}
