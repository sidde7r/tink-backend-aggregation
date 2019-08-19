package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtPayload;

@JsonObject
public class JwtAuthPayload implements JwtPayload {

    private String scope;
    private IdTokenClaim claims;
    private String iss;
    private String redirectUri;
    private String state;
    private String nonce;
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
