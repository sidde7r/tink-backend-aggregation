package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid;

import lombok.SneakyThrows;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;

public class EidasKidProvider implements KidProvider {

    private final String base64EncodedSigningCerts;

    public EidasKidProvider(String base64EncodedSigningCerts) {
        this.base64EncodedSigningCerts = base64EncodedSigningCerts;
    }

    @Override
    @SneakyThrows
    public String get() {
        return CertificateUtils.getOrganizationIdentifier(base64EncodedSigningCerts);
    }
}
