package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoSessionHandler implements SessionHandler {

    private final EvoBancoApiClient apiClient;
    private final SessionStorage sessionStorage;

    public EvoBancoSessionHandler(EvoBancoApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.logout(sessionStorage);
    }

    @Override
    public void keepAlive() throws SessionException {
        if(!apiClient.isAlive(sessionStorage)){
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
