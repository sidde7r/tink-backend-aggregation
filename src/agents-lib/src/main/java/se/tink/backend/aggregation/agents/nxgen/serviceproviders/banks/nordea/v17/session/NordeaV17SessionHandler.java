package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaV17SessionHandler implements SessionHandler {
    private final NordeaV17ApiClient client;

    public NordeaV17SessionHandler(NordeaV17ApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
