package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class SibsConfiguration implements ClientConfiguration {

    @Secret private String baseUrl;
    @Secret private String certificateId;
    @Secret private String clientId;
    @SensitiveSecret private String clientSecret;
    @Secret private String redirectUrl;
    @Secret private String clientSigningCertificate;
    @Secret private String clientSigningCertificateSerialNumber;
    @Secret private String aspspCode;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientSigningCertificate() {
        return clientSigningCertificate;
    }

    public String getClientSigningCertificateSerialNumber() {
        return clientSigningCertificateSerialNumber;
    }

    public String getAspspCode() {
        return aspspCode;
    }
}
