package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordnetBaseSessionHandler implements SessionHandler {

    private final NordnetBaseApiClient apiClient;

    public NordnetBaseSessionHandler(NordnetBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.authorizeSession();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && e.getResponse()
                            .getBody(String.class)
                            .contains(NordnetBaseConstants.Errors.INVALID_SESSION)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            // Re-throw unknown exception
            throw e;
        }
    }
}
