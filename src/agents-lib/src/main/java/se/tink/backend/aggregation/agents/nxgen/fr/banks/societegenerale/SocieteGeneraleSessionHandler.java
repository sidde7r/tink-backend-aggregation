package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SocieteGeneraleSessionHandler implements SessionHandler {
    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        throw new SessionException(SessionError.SESSION_EXPIRED);
    }
}
