package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordnetSessionHandler implements SessionHandler {

    private final NordnetApiClient apiClient;

    public NordnetSessionHandler(NordnetApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.authorizeSession();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && e.getResponse()
                            .getBody(String.class)
                            .contains(NordnetConstants.Errors.INVALID_SESSION)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            // Re-throw unknown exception
            throw e;
        }
    }
}
