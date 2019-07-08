package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.Form;

@JsonObject
public class AuthorizationCodeTokenRequest {
    @JsonProperty("client_id")
    private String clientId = null;

    @JsonProperty("grant_type")
    private GrantTypeEnum grantType = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("redirect_uri")
    private String redirectUri = null;

    @JsonProperty("code_verifier")
    private String codeVerifier = null;

    public AuthorizationCodeTokenRequest(
            String clientId,
            GrantTypeEnum grantType,
            String code,
            String redirectUri,
            String codeVerifier) {
        this.clientId = clientId;
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.codeVerifier = codeVerifier;
    }

    public String toData() {
        // TODO Change
        return Form.builder()
                .put(FormValues.GRANT_TYPE, grantType.toString())
                .put(FormValues.CLIENT_ID, clientId)
                .put(FormValues.CODE, code)
                .put(FormValues.CODE_VERIFIER, codeVerifier)
                .build()
                .serialize();
    }
}
