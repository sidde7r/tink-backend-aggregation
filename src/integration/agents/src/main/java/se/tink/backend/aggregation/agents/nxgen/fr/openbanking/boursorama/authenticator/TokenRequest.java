package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenRequest {

    @JsonProperty("GrantType")
    private String grantType;

    @JsonProperty("AuthorizationCode")
    private String authorizationCode;

    @JsonProperty("ClientId")
    private String clientId;

    public TokenRequest(String grantType, String authorizationCode, String clientId) {
        this.grantType = grantType;
        this.authorizationCode = authorizationCode;
        this.clientId = clientId;
    }
}
