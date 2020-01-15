package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class IngSessionHandler implements SessionHandler {

    @NonNull private final SessionStorage sessionStorage;

    @Override
    public void logout() {
        sessionStorage.clear();
        // API does not support logout
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
