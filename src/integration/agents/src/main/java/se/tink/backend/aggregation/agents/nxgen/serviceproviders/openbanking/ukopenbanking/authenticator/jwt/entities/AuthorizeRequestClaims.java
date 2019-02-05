package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.jwt.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequestClaims {
    private UserinfoEntity userinfo;
    @JsonProperty("id_token")
    private IdTokenEntity idToken;

    public AuthorizeRequestClaims(String intentId, String... acrValues) {
        this.userinfo = new UserinfoEntity(intentId);
        this.idToken = new IdTokenEntity(intentId, acrValues);
    }
}
