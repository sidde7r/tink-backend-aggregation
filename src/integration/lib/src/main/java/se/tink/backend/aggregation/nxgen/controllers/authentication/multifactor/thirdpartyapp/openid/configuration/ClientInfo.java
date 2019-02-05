package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {

    private String clientId;
    private String clientSecret;
    private long clientIdIssuedAt;
    private long clientSecretExpiresAt;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Instant getClientIdIssuedAt() {
        return Instant.ofEpochMilli(clientIdIssuedAt);
    }

    public Instant getClientSecretExpiresAt() {
        return Instant.ofEpochMilli(clientSecretExpiresAt);
    }
}
