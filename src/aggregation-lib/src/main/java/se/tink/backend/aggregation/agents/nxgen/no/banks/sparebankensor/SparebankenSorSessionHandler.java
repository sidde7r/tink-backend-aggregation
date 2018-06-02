package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SparebankenSorSessionHandler implements SessionHandler {
    @Override
    public void logout() {
        //NO OP
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
