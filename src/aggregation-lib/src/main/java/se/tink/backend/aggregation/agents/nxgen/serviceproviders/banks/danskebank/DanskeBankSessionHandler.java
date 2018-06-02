package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DanskeBankSessionHandler implements SessionHandler {

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
