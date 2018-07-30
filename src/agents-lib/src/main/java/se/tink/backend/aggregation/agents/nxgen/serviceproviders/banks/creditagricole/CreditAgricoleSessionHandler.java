package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CreditAgricoleSessionHandler implements SessionHandler {

    private final CreditAgricoleApiClient apiClient;

    public CreditAgricoleSessionHandler(
            CreditAgricoleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (apiClient.keepAlive().getErrors().size() > 0) {
                throw new SessionException(SessionError.SESSION_EXPIRED);
            }
        } catch (IllegalStateException e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
