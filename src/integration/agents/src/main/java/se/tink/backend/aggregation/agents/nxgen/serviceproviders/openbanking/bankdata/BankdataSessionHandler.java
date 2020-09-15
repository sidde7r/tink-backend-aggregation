package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BankdataSessionHandler implements SessionHandler {
    private final BankdataApiClient apiClient;

    public BankdataSessionHandler(BankdataApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        // Nothing to do here, their api doesn't mention an enpoint for that
    }

    @Override
    public void keepAlive() throws SessionException {
        apiClient.getOauthToken();
    }
}
