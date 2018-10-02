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

    public ZonedDateTime getClientIdIssuedAt() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(clientIdIssuedAt), ZoneOffset.UTC);
    }

    public ZonedDateTime getClientSecretExpiresAt() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(clientSecretExpiresAt), ZoneOffset.UTC);
    }
}
