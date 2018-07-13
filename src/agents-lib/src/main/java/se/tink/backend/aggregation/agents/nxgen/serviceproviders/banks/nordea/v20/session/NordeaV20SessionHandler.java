package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaV20SessionHandler implements SessionHandler {
    private final NordeaV20ApiClient client;

    public NordeaV20SessionHandler(NordeaV20ApiClient client) {
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
