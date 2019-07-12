package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenRequest {

    @JsonProperty("grant_type")
    private String grantType;

    private String code;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    private String scope;
    private String ssn;
    private String ipidfr;

    public TokenRequest(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String clientSecret,
            String scope,
            String ssn,
            String ipidfr) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.ssn = ssn;
        this.ipidfr = ipidfr;
    }
}
