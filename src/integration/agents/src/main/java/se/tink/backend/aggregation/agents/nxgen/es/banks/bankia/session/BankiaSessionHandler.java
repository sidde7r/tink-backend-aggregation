package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class BankiaSessionHandler implements SessionHandler {

    private final BankiaApiClient apiClient;

    public BankiaSessionHandler(BankiaApiClient apiClient) {
        super();
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.getDisconnect();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.authorizeSession()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
