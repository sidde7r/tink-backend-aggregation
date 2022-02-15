package se.tink.backend.aggregation.agents.tools.jwt.kid;

import lombok.SneakyThrows;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

public class EidasKeyIdProvider implements KeyIdProvider {

    private final String base64EncodedSigningCerts;

    public EidasKeyIdProvider(String base64EncodedSigningCerts) {
        this.base64EncodedSigningCerts = base64EncodedSigningCerts;
    }

    @Override
    @SneakyThrows
    public String get() {
        return CertificateUtils.getOrganizationIdentifier(base64EncodedSigningCerts);
    }
}
