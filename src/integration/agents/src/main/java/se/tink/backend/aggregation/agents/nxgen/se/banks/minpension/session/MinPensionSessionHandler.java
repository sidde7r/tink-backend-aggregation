package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class MinPensionSessionHandler implements SessionHandler {
    private final MinPensionApiClient minPensionApiClient;

    @Override
    public void keepAlive() throws SessionException {
        try {
            minPensionApiClient.fetchSsn();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }
}
