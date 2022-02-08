package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class NordeaDkAutoAuthenticator implements AutoAuthenticator {

    private static final String INVALID_GRANT = "invalid_grant";

    private final NordeaDkApiClient bankClient;
    private final PersistentStorage persistentStorage;
    private final NordeaDkAuthenticatorUtils authenticatorUtils;

    @Override
    public void autoAuthenticate() {
        try {
            OAuth2Token token =
                    getToken().orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            String refreshToken =
                    token.getOptionalRefreshToken()
                            .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            OAuth2Token newToken =
                    bankClient
                            .exchangeRefreshToken(refreshToken)
                            .toOauthToken()
                            .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
            authenticatorUtils.saveToken(newToken);
        } catch (HttpResponseException e) {
            String error = (String) e.getResponse().getBody(Map.class).get("error");
            if (e.getResponse().getStatus() == 400 && INVALID_GRANT.equals(error)) {
                // refresh token expired
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        } catch (AuthenticationException e) {
            log.info("Refresh token missing or invalid, proceeding to manual authentication");
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private Optional<OAuth2Token> getToken() {
        return persistentStorage.get(NordeaDkConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class);
    }
}
