package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientInfo {

    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("client_id_issued_at")
    private long issuedAt;
    @JsonProperty("client_secret_expires_at")
    private long expiresAt;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public ZonedDateTime getIssuedAt() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(issuedAt), ZoneOffset.UTC);
    }

    public ZonedDateTime getExpiresAt() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(expiresAt), ZoneOffset.UTC);
    }
}
