package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtSessionExpiredParser;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IngAtSessionHandler implements SessionHandler {
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage ingAtSessionStorage;

    public IngAtSessionHandler(
            final IngAtApiClient apiClient, final IngAtSessionStorage ingAtSessionStorage) {
        this.apiClient = apiClient;
        this.ingAtSessionStorage = ingAtSessionStorage;
    }

    @Override
    public void logout() {
        ingAtSessionStorage.clear();
        apiClient.logOut();
    }

    @Override
    public void keepAlive() throws SessionException {
        // If nothing in session storage -> SessionException
        ingAtSessionStorage
                .getWebLoginResponse()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        // If keepalive request fails -> SessionException
        final HttpResponse keepAliveResponse;
        try {
            keepAliveResponse =
                    apiClient.keepAlive().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        // If keepalive response indicates expiration -> SessionException
        if (keepAliveResponse.getRequest().getUrl().get().contains("timeExpired")) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        if (new IngAtSessionExpiredParser(keepAliveResponse.getBody(String.class)).isSessionExpired()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
