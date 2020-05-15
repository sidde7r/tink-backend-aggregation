package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.time.Instant;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {

    private String clientId;
    private String clientSecret;
    private long clientIdIssuedAt;
    private long clientSecretExpiresAt;
    private String tokenEndpointAuthMethod;

    public String getTokenEndpointAuthMethod() {
        return tokenEndpointAuthMethod;
    }

    public ClientInfo() {}

    public ClientInfo(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public ClientInfo(String clientId, String clientSecret, String tokenEndpointAuthMethod) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    // TODO: No usage of clientIdIssuedAt and clientSecretExpiresAt, should be removed...
    public Instant getClientIdIssuedAt() {
        return Instant.ofEpochMilli(clientIdIssuedAt);
    }

    public Instant getClientSecretExpiresAt() {
        return Instant.ofEpochMilli(clientSecretExpiresAt);
    }
}
