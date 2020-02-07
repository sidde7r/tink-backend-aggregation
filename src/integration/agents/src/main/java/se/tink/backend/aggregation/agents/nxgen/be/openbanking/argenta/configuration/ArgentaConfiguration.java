package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class ArgentaConfiguration implements ClientConfiguration {

    @Secret private String clientId;
    @Secret private String keyId;
    @Secret private String redirectUrl;
    @Secret private String clientSigningCertificate;
    @Secret private String psuIpAddress;
    @SensitiveSecret private String apiKey;

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getClientSigningCertificate() {
        return clientSigningCertificate;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }

    public String getKeyId() {
        return keyId;
    }
}
