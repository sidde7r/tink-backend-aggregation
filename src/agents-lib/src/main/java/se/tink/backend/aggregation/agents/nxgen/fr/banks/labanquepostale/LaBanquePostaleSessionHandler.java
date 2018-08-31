package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import java.util.Optional;
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
    }

    @Override
    public void keepAlive() throws SessionException {

        Optional<String> errorCode = apiClient.isAlive();

        if (errorCode.isPresent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
