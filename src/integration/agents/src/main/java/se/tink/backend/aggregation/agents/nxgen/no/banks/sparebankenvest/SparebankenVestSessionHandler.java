package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SparebankenVestSessionHandler implements SessionHandler {
    private final SparebankenVestApiClient apiClient;

    private SparebankenVestSessionHandler(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestSessionHandler create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestSessionHandler(apiClient);
    }

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (!apiClient.keepAlive()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
