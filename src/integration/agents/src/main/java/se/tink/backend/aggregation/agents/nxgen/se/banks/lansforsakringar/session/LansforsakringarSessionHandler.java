package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class LansforsakringarSessionHandler implements SessionHandler {
    private final LansforsakringarApiClient apiClient;

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.renewSession();
        } catch (HttpResponseException ex) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
