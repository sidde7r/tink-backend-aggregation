package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration;

public class Xs2aDevelopersProviderConfiguration {
    private String baseUrl;
    private String clientId;
    private String redirectUrl;

    public Xs2aDevelopersProviderConfiguration(
            String clientId, String baseUrl, String redirectUrl) {
        this.clientId = clientId;
        this.baseUrl = baseUrl;
        this.redirectUrl = redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
