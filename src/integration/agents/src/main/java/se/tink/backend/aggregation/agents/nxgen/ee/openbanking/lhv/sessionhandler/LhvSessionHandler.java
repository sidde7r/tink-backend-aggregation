package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.sessionhandler;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AllArgsConstructor
public class LhvSessionHandler implements SessionHandler {

    private final LhvApiClient apiClient;

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
