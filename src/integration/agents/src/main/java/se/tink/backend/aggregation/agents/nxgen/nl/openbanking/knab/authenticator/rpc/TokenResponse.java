package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class TokenResponse {

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String scope;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.createBearer(accessToken, refreshToken, expiresIn);
    }

    public boolean userHasAuthorizedPsd2Consent() {
        return StringUtils.containsIgnoreCase(
                Strings.nullToEmpty(scope), KnabConstants.Scopes.PSD2);
    }
}
