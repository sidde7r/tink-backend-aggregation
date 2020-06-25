package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity;

public class ClientConfigurationEntity {

    private String clientId;
    private String clientSecret;

    public ClientConfigurationEntity(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
