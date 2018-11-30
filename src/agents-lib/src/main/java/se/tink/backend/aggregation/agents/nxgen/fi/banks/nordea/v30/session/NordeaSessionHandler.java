package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSessionHandler implements SessionHandler {

    private final NordeaFiApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaSessionHandler(
            NordeaFiApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        AuthenticateResponse response = apiClient.keepAlive();
        response.storeTokens(sessionStorage);
    }
}
