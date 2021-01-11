package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants.Storage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorSessionHandler implements SessionHandler {
    private final CollectorApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CollectorSessionHandler(CollectorApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!sessionStorage.containsKey(Storage.BEARER_TOKEN)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            final String token = sessionStorage.get(CollectorConstants.Storage.BEARER_TOKEN);
            apiClient.fetchAccounts(token);
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
