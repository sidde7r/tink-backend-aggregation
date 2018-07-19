package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class CommerzbankSessionHandler implements SessionHandler {

    private CommerzbankApiClient apiClient;

    public CommerzbankSessionHandler(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.keepAlive();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
