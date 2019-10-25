package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class SpankkiSessionHandler implements SessionHandler {
    private final SpankkiApiClient apiClient;

    public SpankkiSessionHandler(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        // TODO: Implement
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (!apiClient.keepAlive().getStatus().isSessionExpired()) {
                return;
            }
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        throw SessionError.SESSION_EXPIRED.exception();
    }
}
