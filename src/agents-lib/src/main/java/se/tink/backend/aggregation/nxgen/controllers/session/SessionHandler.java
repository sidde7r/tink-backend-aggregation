package se.tink.backend.aggregation.nxgen.controllers.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;

public interface SessionHandler {
    void logout();
    void keepAlive() throws SessionException;
}
