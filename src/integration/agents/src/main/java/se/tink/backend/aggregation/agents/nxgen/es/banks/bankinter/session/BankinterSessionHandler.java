package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class BankinterSessionHandler implements SessionHandler {

    private final BankinterApiClient apiClient;

    public BankinterSessionHandler(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.keepAlive()) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
