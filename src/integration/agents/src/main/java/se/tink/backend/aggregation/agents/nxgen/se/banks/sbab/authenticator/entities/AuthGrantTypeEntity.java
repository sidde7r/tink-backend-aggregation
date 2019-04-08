package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum AuthGrantTypeEntity {
    @JsonProperty("client_credentials")
    CLIENT_CREDENTIALS,
    @JsonProperty("pending_authorization_code")
    PENDING_AUTHORIZATION_CODE,
    @JsonProperty("refresh_token")
    REFRESH_TOKEN,
}
