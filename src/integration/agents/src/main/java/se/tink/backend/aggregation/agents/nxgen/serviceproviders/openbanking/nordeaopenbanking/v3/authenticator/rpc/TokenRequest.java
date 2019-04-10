package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenRequest {
    @JsonProperty("grant_type")
    private String grantType = NordeaBaseConstants.Authorization.GRANT_TYPE_AUTH_CODE;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    private String code;

    public TokenRequest(String code, String redirectUri) {
        this.code = code;
        this.redirectUri = redirectUri;
    }
}
