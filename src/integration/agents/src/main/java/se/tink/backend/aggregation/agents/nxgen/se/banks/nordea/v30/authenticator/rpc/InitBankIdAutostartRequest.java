package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants.TagValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdAutostartRequest {
    private String scope = "openid ndf agreement offline_access";
    private String state = "";
    private String nonce = "";

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

    public InitBankIdAutostartRequest setState(String state) {
        this.state = state;
        return this;
    }

    public InitBankIdAutostartRequest setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public InitBankIdAutostartRequest setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
        return this;
    }
}
