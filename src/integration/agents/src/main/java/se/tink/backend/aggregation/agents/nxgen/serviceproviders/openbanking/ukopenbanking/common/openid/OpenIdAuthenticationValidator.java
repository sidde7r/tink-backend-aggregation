package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import java.security.PublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.TokenValidationResult;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator.IdTokenValidator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
@Slf4j
public class OpenIdAuthenticationValidator {

    private static final String CODE_AND_STATE_MSG = "code and state";
    private static final String ACCESS_TOKEN_MSG = "access token";

    private final OpenIdApiClient apiClient;

    public void validateIdToken(String idToken, String authCode, String state) {
        final TokenValidationResult validationResult =
                apiClient
                        .getJwkPublicKeys()
                        .map(
                                publicKeys ->
                                        getIdTokenValidationResult(
                                                idToken, authCode, state, publicKeys))
                        .orElse(TokenValidationResult.NOT_POSSIBLE);

        logIdTokenValidationResult(validationResult, CODE_AND_STATE_MSG);
    }

    public void validateClientToken(OAuth2Token clientOAuth2Token) {
        if (!clientOAuth2Token.isValid()) {
            throw new IllegalArgumentException("Client access token is not valid.");
        }
    }

    public void validateAccessToken(OAuth2Token oAuth2Token) {
        if (!oAuth2Token.isValid()) {
            throw new IllegalArgumentException("Invalid access token.");
        }

        if (!oAuth2Token.isBearer()) {
            throw new IllegalArgumentException(
                    String.format("Unknown token type '%s'.", oAuth2Token.getTokenType()));
        }

        if (oAuth2Token.getIdToken() != null) {
            validateIdToken(oAuth2Token.getIdToken(), oAuth2Token.getAccessToken());
        } else {
            log.warn("ID Token (access token) validation - no token provided");
        }
    }

    private void validateIdToken(String idToken, String accessToken) {
        final TokenValidationResult validationResult =
                apiClient
                        .getJwkPublicKeys()
                        .map(
                                publicKeys ->
                                        getIdTokenValidationResult(
                                                idToken, accessToken, publicKeys))
                        .orElse(TokenValidationResult.NOT_POSSIBLE);

        logIdTokenValidationResult(validationResult, ACCESS_TOKEN_MSG);
    }

    private static TokenValidationResult getIdTokenValidationResult(
            String idToken, String authCode, String state, Map<String, PublicKey> publicKeys) {
        return new IdTokenValidator(idToken, publicKeys)
                .withCHashValidation(authCode)
                .withSHashValidation(state)
                .withMode(IdTokenValidator.ValidatorMode.LOGGING)
                .execute();
    }

    private static TokenValidationResult getIdTokenValidationResult(
            String idToken, String accessToken, Map<String, PublicKey> publicKeys) {
        return new IdTokenValidator(idToken, publicKeys)
                .withAtHashValidation(accessToken)
                .withMode(IdTokenValidator.ValidatorMode.LOGGING)
                .execute();
    }

    private static void logIdTokenValidationResult(
            TokenValidationResult validationResult, String msg) {
        switch (validationResult) {
            case SUCCESS:
                log.info("ID Token (" + msg + ") validation successful");
                break;
            case FAILURE:
                log.info("ID Token (" + msg + ") validation failed");
                break;
            default:
                log.warn("ID Token (" + msg + ") validation not possible - no public keys");
                break;
        }
    }
}
