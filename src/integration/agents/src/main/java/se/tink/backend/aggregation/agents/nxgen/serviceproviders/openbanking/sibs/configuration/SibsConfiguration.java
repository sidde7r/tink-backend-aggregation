package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SibsConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @Secret private String clientSigningCertificate;
    @Secret private String clientSigningCertificateSerialNumber;

    public String getClientId() {
        return clientId;
    }

    public String getClientSigningCertificate() {
        return clientSigningCertificate;
    }

    public String getClientSigningCertificateSerialNumber() {
        return clientSigningCertificateSerialNumber;
    }
}
