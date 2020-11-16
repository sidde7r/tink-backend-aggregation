package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@RequiredArgsConstructor
@Slf4j
public class NordeaNoSessionHandler implements SessionHandler {

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;

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
