package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaV21SessionHandler implements SessionHandler {
    private final NordeaV21ApiClient client;

    public NordeaV21SessionHandler(NordeaV21ApiClient client) {
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
