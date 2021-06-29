package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class BecSessionHandler implements SessionHandler {

    private final BecApiClient apiClient;

    public BecSessionHandler(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            log.warn("Caught exception while checking if session is active", e);
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
