package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.IsAuthenticatedResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class AsLhvSessionHandler implements SessionHandler {

    private AsLhvApiClient asLhvApiClient;

    public AsLhvSessionHandler(AsLhvApiClient asLhvApiClient) {
        this.asLhvApiClient = asLhvApiClient;
    }

    @Override
    public void logout() {
        asLhvApiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        IsAuthenticatedResponse isAuthenticatedResponse = asLhvApiClient.isAuthenticated();
        if (!isAuthenticatedResponse.isAuthenticated()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
