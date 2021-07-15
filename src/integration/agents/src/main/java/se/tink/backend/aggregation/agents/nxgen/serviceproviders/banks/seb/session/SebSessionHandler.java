package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SebSessionHandler implements SessionHandler {
    private SebApiClient apiClient;
    private SebSessionStorage sebSessionStorage;

    public SebSessionHandler(SebApiClient apiClient, SebSessionStorage sebSessionStorage) {
        this.apiClient = apiClient;
        this.sebSessionStorage = sebSessionStorage;
    }

    @Override
    public void logout() {}

    private boolean isLoggedIn() {
        final String customerNumber = sebSessionStorage.getCustomerNumber();
        if (Strings.isNullOrEmpty(customerNumber)) {
            return false;
        }

        try {
            // The app only uses this endpoint when authenticating, calling it again can be used
            // to check if the session is still active
            final Response response = apiClient.activateSession();
            return response.isValid()
                    && customerNumber.equals(response.getUserInformation().getSebCustomerNumber());
        } catch (HttpClientException | HttpResponseException e) {
            return false;
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!isLoggedIn()) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
