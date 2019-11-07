package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import static java.util.Objects.requireNonNull;

public class NovoBancoSessionHandler implements SessionHandler {

    private final NovoBancoApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NovoBancoSessionHandler(NovoBancoApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = requireNonNull(apiClient);
        this.sessionStorage = requireNonNull(sessionStorage);
    }

    @Override
    public void logout() {
        sessionStorage.clear();
        // API does not support logout
    }

    @Override
    public void keepAlive() throws SessionException {
         if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
