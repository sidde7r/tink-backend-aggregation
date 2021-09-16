package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CitadeleSessionHandler implements SessionHandler {

    private CitadeleBaseApiClient apiClient;

    public CitadeleSessionHandler(CitadeleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!ConsentStatus.VALID.equals(apiClient.getConsentStatus())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
