package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@RequiredArgsConstructor
final class MetroSessionHandler implements SessionHandler {

    private final SessionClient client;

    @Override
    public void logout() {
        client.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (client.isSessionExpired()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
