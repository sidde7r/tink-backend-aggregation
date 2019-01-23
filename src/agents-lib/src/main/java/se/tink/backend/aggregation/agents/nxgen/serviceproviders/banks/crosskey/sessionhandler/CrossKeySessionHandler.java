package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.rpc.KeepAliveResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CrossKeySessionHandler implements SessionHandler {

    private final CrossKeyApiClient client;

    public CrossKeySessionHandler(CrossKeyApiClient client) {
        this.client = client;
    }

    @Override
    public void logout() {
        client.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        KeepAliveResponse response = client.keepAlive();
        if (response.isFailure()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
