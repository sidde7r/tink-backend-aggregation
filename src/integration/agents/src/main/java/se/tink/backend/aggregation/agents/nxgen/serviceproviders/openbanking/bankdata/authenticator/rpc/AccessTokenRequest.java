package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class AccessTokenRequest implements TokenRequest {

    @JsonProperty("grant_type")
    private final String grantType;

    @JsonProperty("client_id")
    private final String clientId;

    private String code;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("code_verifier")
    private String codeVerifier;

    public AccessTokenRequest(
            String grantType,
            String clientId,
            String code,
            String redirectUri,
            String codeVerifier) {
        this.grantType = grantType;
        this.clientId = clientId;
        this.code = code;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        return Form.builder()
                .put(QueryKeys.GRANT_TYPE, grantType)
                .put(QueryKeys.CLIENT_ID, clientId)
                .put(QueryKeys.CODE, code)
                .put(QueryKeys.REDIRECT_URI, redirectUri)
                .put(QueryKeys.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
