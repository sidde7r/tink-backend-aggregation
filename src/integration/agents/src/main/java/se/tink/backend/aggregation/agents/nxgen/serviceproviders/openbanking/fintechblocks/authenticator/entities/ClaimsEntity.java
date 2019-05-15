package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClaimsEntity {

    @JsonProperty("id_token")
    private ClaimsInfoEntity idToken;

    private ClaimsInfoEntity userinfo;

    public ClaimsEntity(ClaimsInfoEntity idToken, ClaimsInfoEntity userinfo) {
        this.idToken = idToken;
        this.userinfo = userinfo;
    }
}
