package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthJWTPayload {

    private ClaimsEntity claims;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    public AuthJWTPayload(ClaimsEntity claims, String clientId, String redirectUri) {
        this.claims = claims;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }
}
