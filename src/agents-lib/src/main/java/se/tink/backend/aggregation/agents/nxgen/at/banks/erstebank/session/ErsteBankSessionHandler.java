package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class ErsteBankSessionHandler implements SessionHandler {
    private final ErsteBankApiClient ersteBankApiClient;

    public ErsteBankSessionHandler(ErsteBankApiClient ersteBankApiClient) {
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public void logout() {
        ersteBankApiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!ersteBankApiClient.tokenExists()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            ersteBankApiClient.fetchAccounts();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
