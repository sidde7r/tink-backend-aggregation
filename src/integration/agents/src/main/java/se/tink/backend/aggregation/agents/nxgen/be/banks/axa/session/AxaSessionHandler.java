package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class AxaSessionHandler implements SessionHandler {

    private AxaApiClient apiClient;
    private AxaStorage storage;

    public AxaSessionHandler(final AxaApiClient apiClient, final AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        final int customerId =
                storage.getCustomerId().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        final String accessToken =
                storage.getAccessToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        try {
            apiClient.postPendingRequests(customerId, accessToken);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw SessionError.SESSION_EXPIRED.exception();
            } else {
                throw e;
            }
        } catch (HttpClientException e) {
            if (e.getMessage().contains("failed to respond")) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
    }
}
