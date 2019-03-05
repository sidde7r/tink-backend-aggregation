package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DemoFakeBankSessionHandler implements SessionHandler {
    private DemoFakeBankApiClient client;

    public DemoFakeBankSessionHandler(DemoFakeBankApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {
        //TODO: Logout
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
