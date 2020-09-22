package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaSESessionHandler implements SessionHandler {
    private final NordeaSEApiClient apiClient;

    public NordeaSESessionHandler(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        apiClient.keepAlive();
    }
}
