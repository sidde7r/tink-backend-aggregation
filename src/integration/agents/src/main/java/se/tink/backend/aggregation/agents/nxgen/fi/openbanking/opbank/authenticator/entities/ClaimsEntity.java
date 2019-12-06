package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClaimsEntity {
    @JsonProperty("userinfo")
    private ClaimEntity userInfo;

    @JsonProperty("id_token")
    private ClaimEntity idToken;

    public ClaimsEntity(ClaimEntity userInfo, ClaimEntity idToken) {
        this.userInfo = userInfo;
        this.idToken = idToken;
    }
}
