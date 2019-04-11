package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class ImaginBankSessionHandler implements SessionHandler {

    private ImaginBankApiClient apiClient;

    public ImaginBankSessionHandler(ImaginBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
