package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthInfoResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SocieteGeneraleSessionHandler implements SessionHandler {

    private final SocieteGeneraleApiClient apiClient;

    public SocieteGeneraleSessionHandler(
            SocieteGeneraleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {

        AuthInfoResponse info = apiClient.getAuthInfo();

        if (!"ok".equalsIgnoreCase(info.getCommon().getStatus())) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }

}
