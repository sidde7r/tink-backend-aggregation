package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Storage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CitiBanaMexSessionHandler implements SessionHandler {

    private final CitiBanaMexApiClient client;
    private final SessionStorage sessionStorage;

    public CitiBanaMexSessionHandler(CitiBanaMexApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        client.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        // check if the user session is even created at our end.
        if (sessionStorage.get(Storage.RSA_APPLICATION_KEY) == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        client.keepAlive();
    }
}
