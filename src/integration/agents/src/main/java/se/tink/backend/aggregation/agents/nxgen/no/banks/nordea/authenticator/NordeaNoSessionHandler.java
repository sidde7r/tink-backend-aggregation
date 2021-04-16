package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@Slf4j
@AllArgsConstructor
public class NordeaNoSessionHandler implements SessionHandler {

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;

    @Override
    public void logout() {
        authenticationClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        String refreshToken =
                storage.retrieveOauthToken()
                        .flatMap(OAuth2TokenBase::getRefreshToken)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        OauthTokenResponse oauthTokenResponse =
                authenticationClient.refreshAccessToken(refreshToken);

        storage.storeOauthToken(
                oauthTokenResponse
                        .toOauthToken()
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception));
    }
}
