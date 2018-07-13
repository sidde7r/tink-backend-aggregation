package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SabadellSessionHandler implements SessionHandler {
    private SabadellApiClient apiClient;

    public SabadellSessionHandler(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        this.apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {

        try {
            apiClient.fetchCreditCards();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
