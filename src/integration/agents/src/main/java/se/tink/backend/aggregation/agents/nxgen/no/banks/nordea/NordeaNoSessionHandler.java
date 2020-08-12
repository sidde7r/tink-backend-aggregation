package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AllArgsConstructor
@Slf4j
public class NordeaNoSessionHandler implements SessionHandler {

    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;

    @Override
    public void logout() {
        authenticationClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        OauthTokenResponse oauthTokenResponse = authenticationClient.refreshAccessToken();

        storage.storeOauthToken(
                oauthTokenResponse
                        .toOauthToken()
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception));
    }
}
