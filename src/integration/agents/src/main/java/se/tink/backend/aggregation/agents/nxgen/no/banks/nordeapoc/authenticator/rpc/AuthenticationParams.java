package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.QueryParamValues;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthenticationParams {

    private String codeChallenge;
    private String nonce;
    private String state;

    private String acrValues = null;
    private String amr = QueryParamValues.METHOD_ID;
    private String clientId = QueryParamValues.CLIENT_ID;
    private String codeChallengeMethod = QueryParamValues.CODE_CHALLENGE_METHOD;
    private String redirectUri = QueryParamValues.REDIRECT_URI_FOR_NORDEA_SESSION_INITIALIZATION;
    private String responseType = QueryParamValues.RESPONSE_TYPE;
    private String scope = QueryParamValues.SCOPE;
    private String signingOrderId = null;

    public AuthenticationParams(String codeChallenge, String nonce, String state) {
        this.codeChallenge = codeChallenge;
        this.nonce = nonce;
        this.state = state;
    }
}
