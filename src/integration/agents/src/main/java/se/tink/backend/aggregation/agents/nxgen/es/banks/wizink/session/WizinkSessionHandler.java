package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class WizinkSessionHandler implements SessionHandler {

    private WizinkApiClient wizinkApiClient;

    public WizinkSessionHandler(WizinkApiClient wizinkApiClient) {
        this.wizinkApiClient = wizinkApiClient;
    }

    @Override
    public void logout() {
        wizinkApiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!wizinkApiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
