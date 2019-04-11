package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Client;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngSessionHandler implements SessionHandler {

    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IngSessionHandler(IngApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        // This is what the app calls when the user actively logs out
        apiClient.getApiRestCommunicationLogoutAllSessions();
        // This, I believe, is called to make sure there are no sessions remaining (before creating
        // a new one)
        apiClient.deleteApiRestSession();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            Client response = apiClient.getApiRestClient();
            if (response == null || response.getId() == null) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException | HttpClientException exception) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
