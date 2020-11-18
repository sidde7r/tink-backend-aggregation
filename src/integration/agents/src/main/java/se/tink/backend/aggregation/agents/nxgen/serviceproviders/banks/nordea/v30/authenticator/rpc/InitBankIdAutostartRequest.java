package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.FormParams;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class InitBankIdAutostartRequest {
    @Builder.Default private String scope = FormParams.SCOPE_OPENID;

    @Builder.Default private String state = "";

    @Builder.Default private String nonce = "";

    @JsonProperty("code_challenge")
    @Builder.Default
    private String codeChallenge = "";

    @JsonProperty("response_type")
    @Builder.Default
    private String responseType = FormParams.CODE;

    @JsonProperty("code_challenge_method")
    @Builder.Default
    private String codeChallengeMethod = FormParams.CHALLENGE_METHOD;

    @JsonProperty("user_id")
    @Builder.Default
    private String userId = "";

    @JsonProperty("redirect_uri")
    @Builder.Default
    private String redirectUri = "";

    @JsonProperty("client_id")
    @Builder.Default
    private String clientId = "";

    @JsonProperty("signing_order_id")
    private String signingOrderId;
}
