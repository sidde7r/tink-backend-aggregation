package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ClaimsEntity {
    @JsonProperty("userinfo")
    private ClaimEntity userInfo;

    @JsonProperty("id_token")
    private ClaimEntity idToken;
}
