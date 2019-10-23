package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity;

public class ClientConfigurationEntity {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;

    public ClientConfigurationEntity(String clientId, String clientSecret, String redirectUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
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
}
