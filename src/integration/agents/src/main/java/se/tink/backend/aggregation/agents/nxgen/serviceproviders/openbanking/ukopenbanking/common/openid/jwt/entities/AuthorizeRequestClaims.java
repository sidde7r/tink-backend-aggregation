package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class AuthorizeRequestClaims {

    private UserInfoEntity userinfo;

    @JsonProperty("id_token")
    private IdTokenEntity idToken;
}
