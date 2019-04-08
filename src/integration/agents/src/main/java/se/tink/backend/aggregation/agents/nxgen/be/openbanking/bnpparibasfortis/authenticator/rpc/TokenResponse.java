
package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenResponse {

    @JsonProperty
    private String expires;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty
    private String refresh;

    @JsonProperty
    private String token;

    @JsonProperty("token_type")
    private String tokenType;

    public String getExpires() {
        return expires;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getRefresh() {
        return refresh;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }
}
