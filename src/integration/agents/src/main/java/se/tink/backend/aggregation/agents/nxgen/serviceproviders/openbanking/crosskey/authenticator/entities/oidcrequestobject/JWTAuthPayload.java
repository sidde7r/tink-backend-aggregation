package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JWTAuthPayload implements JWTPayload{

    private String scope;
    private IdTokenClaim claims;
    private String iss;
    private String redirectUri;
    private String state;
    private String nonce;
    private String clientId;

    public JWTAuthPayload(String scope, IdTokenClaim claims, String iss, String redirectUri,
        String state, String nonce, String clientId) {
        this.scope = scope;
        this.claims = claims;
        this.iss = iss;
        this.redirectUri = redirectUri;
        this.state = state;
        this.nonce = nonce;
        this.clientId = clientId;
    }
}
