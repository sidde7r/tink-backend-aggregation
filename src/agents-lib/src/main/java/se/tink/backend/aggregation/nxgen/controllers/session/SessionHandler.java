package se.tink.backend.aggregation.nxgen.controllers.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;

public interface SessionHandler {

    static SessionHandler alwaysFail() {
        return new NoOpSessionHandler();
    }

    class NoOpSessionHandler implements SessionHandler {

        @Override
        public void logout() {
        }

        @Override
        public void keepAlive() throws SessionException {
            throw SessionError.SESSION_EXPIRED.exception();
        }

    }

    /**
     * Terminates the current session between the client and the server.
     * Postconditions:
     * - SessionStorage is empty
     * - If possible, the server is informed that the client wishes to terminate the session
     */
    void logout();

    /**
     * Checks whether the session is alive. Typically, this means making a "keepalive" request containing the token of
     * the current session, and checking the response.
     *
     * @throws SessionException if and only if any of the following conditions are met:
     *                          - The client does not hold a session token
     *                          - The client holds a session token which is sent to the server, but the response indicates that the session is
     *                          not alive
     */
    void keepAlive() throws SessionException;
}
