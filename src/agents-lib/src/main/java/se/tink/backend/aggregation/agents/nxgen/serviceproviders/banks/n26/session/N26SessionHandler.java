package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.session;

import com.google.api.client.http.HttpStatusCodes;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class N26SessionHandler implements SessionHandler {

    private N26ApiClient n26ApiClient;

    public N26SessionHandler(N26ApiClient n26ApiClient) {
        this.n26ApiClient = n26ApiClient;
    }

    @Override
    public void logout() {
        n26ApiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            HttpResponse response = n26ApiClient.checkIfSessionAlive();

            if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
