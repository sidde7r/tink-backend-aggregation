package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JwtPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JwtAuthPayload implements JwtPayload {

    private String scope;
    private IdTokenClaim claims;
    private String iss;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    private String state;
    private String nonce;

    @JsonProperty("client_id")
    private String clientId;

    public JwtAuthPayload(
            String scope,
            IdTokenClaim claims,
            String iss,
            String redirectUri,
            String state,
            String nonce,
            String clientId) {
        this.scope = scope;
        this.claims = claims;
        this.iss = iss;
        this.redirectUri = redirectUri;
        this.state = state;
        this.nonce = nonce;
        this.clientId = clientId;
    }
}
