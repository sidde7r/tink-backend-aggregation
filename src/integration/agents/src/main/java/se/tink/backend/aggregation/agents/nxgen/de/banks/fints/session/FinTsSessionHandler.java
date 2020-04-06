package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class FinTsSessionHandler implements SessionHandler {

    public FinTsSessionHandler() {}

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
