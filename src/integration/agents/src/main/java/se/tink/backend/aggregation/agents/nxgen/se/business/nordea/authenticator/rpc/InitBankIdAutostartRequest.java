package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.TagValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdAutostartRequest {
    @JsonProperty("scope")
    private String scope = "openid ndf agreement offline_access";

    public final String state;
    public final String nonce;

    @JsonProperty("code_challenge")
    private String codeChallenge = "";

    @JsonProperty("response_type")
    private String responseType = FormParams.CODE;

    @JsonProperty("code_challenge_method")
    private String codeChallengeMethod = "S256";

    @JsonProperty("user_id")
    private String userId = "";

    @JsonProperty("redirect_uri")
    private String redirectUri = TagValues.REDIRECT_URI;

    @JsonProperty("client_id")
    private String clientId = TagValues.APPLICATION_ID;

    public InitBankIdAutostartRequest(String state, String nonce, String codeChallenge) {
        this.state = state;
        this.nonce = nonce;
        this.codeChallenge = codeChallenge;
    }
}
