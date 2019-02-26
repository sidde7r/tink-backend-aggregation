package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ClientResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IngSessionHandler implements SessionHandler {

    private IngApiClient apiClient;

    public IngSessionHandler(IngApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.getApiRestCommunicationLogoutRequest();
        apiClient.deleteApiRestSession();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            ClientResponse response = apiClient.getApiRestClient();
            if (response == null || response.getId() == null) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException | HttpClientException exception) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
