package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class ChebancaConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String certificateId;
    private String applicationId;
    private String baseUrl;

    public ChebancaConfiguration() {}

    public ChebancaConfiguration(
            String clientId,
            String clientSecret,
            String redirectUrl,
            String certificateId,
            String applicationId,
            String baseUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.certificateId = certificateId;
        this.applicationId = applicationId;
        this.baseUrl = baseUrl;
    }

    public String getApplicationId() {
        return applicationId;
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

    public String getBaseUrl() {
        return baseUrl;
    }
}
