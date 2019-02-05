package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class LaBanquePostaleSessionHandler implements SessionHandler {

    private final LaBanquePostaleApiClient apiClient;

    public LaBanquePostaleSessionHandler(
            LaBanquePostaleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.getDisconnection();
    }

    @Override
    public void keepAlive() throws SessionException {

        if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
