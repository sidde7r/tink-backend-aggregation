package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BBVASessionHandler implements SessionHandler {

    private final BBVAApiClient client;

    public BBVASessionHandler(BBVAApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            client.fetchAccounts();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
