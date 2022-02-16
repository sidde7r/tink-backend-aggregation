package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import java.security.PublicKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.TokenValidationResult;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator.IdTokenValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator.IdTokenValidator.ValidatorMode;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
@Slf4j
public class OpenIdAuthenticationValidator {

    private final OpenIdApiClient apiClient;

    public void validateIdToken(String idToken, String code, String state) {
        validateWithIdTokenValidator(
                new IdTokenValidator(idToken).withCHashValidation(code).withSHashValidation(state));
    }

    public void validateToken(OAuth2Token oAuth2Token, String msg) {
        if (!oAuth2Token.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception(
                    String.format("[OpenIdAuthenticationValidator] Invalid %s.", msg));
        }
    }

    public void validateRefreshableAccessToken(OAuth2Token oAuth2Token) {
        validateToken(oAuth2Token, Token.ACCESS_TOKEN_MSG);

        if (!oAuth2Token.isBearer()) {
            throw new IllegalArgumentException(
                    String.format(
                            "[OpenIdAuthenticationValidator] Unknown token type '%s'.",
                            oAuth2Token.getTokenType()));
        }

        if (oAuth2Token.isRefreshNullOrEmpty()) {
            log.warn(
                    "[OpenIdAuthenticationValidator] Missing refresh token. Something might be wrong. Verify scope parameter and/or consider contacting the bank");
        }

        if (oAuth2Token.getIdToken() != null) {
            validateIdToken(oAuth2Token);
        } else {
            log.warn(
                    "[OpenIdAuthenticationValidator] ID Token (access token) validation - no token provided");
        }
    }

    private void validateIdToken(OAuth2Token oAuth2Token) {
        validateWithIdTokenValidator(
                new IdTokenValidator(oAuth2Token.getIdToken())
                        .withAtHashValidation(oAuth2Token.getAccessToken()));
    }

    private void validateWithIdTokenValidator(IdTokenValidator idTokenValidator) {
        final TokenValidationResult validationResult =
                apiClient
                        .getJwkPublicKeys()
                        .map(publicKeys -> getIdTokenValidationResult(idTokenValidator, publicKeys))
                        .orElse(TokenValidationResult.NOT_POSSIBLE);

        if (idTokenValidator.isValidationWithAccessToken()) {
            logIdTokenValidationResult(validationResult, Token.ACCESS_TOKEN_MSG);
        } else {
            logIdTokenValidationResult(validationResult, Token.CODE_AND_STATE_MSG);
        }
    }

    private TokenValidationResult getIdTokenValidationResult(
            IdTokenValidator validator, Map<String, PublicKey> publicKeys) {
        return validator.withPublicKeys(publicKeys).withMode(ValidatorMode.LOGGING).execute();
    }

    private void logIdTokenValidationResult(TokenValidationResult validationResult, String msg) {
        String idTokenMsg = "[OpenIdAuthenticationValidator] ID Token " + msg;
        switch (validationResult) {
            case SUCCESS:
                log.info("{} validation successful", idTokenMsg);
                break;
            case FAILURE:
                log.info("{} validation failed", idTokenMsg);
                break;
            default:
                log.warn("{} validation not possible - no public keys", idTokenMsg);
                break;
        }
    }
}
