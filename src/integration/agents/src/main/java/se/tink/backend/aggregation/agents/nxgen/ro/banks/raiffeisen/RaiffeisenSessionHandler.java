package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class RaiffeisenSessionHandler implements SessionHandler {
    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
