package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SpankkiSessionHandler implements SessionHandler {

    private final SpankkiApiClient apiClient;

    public SpankkiSessionHandler(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            SpankkiResponse response = apiClient.fetchAccounts();
            if (response.isLoggedIn()) {
                return;
            }
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }
}
