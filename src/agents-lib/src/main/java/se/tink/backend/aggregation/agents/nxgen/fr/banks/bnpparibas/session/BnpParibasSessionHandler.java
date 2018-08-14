package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BnpParibasSessionHandler implements SessionHandler {
    private final BnpParibasApiClient apiClient;

    public BnpParibasSessionHandler(BnpParibasApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.keepAlive();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
