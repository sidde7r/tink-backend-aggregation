package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.sessionhandler;

import com.google.api.client.http.HttpStatusCodes;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class Sparebank1SessionHandler implements SessionHandler {

    private final Sparebank1ApiClient apiClient;

    public Sparebank1SessionHandler(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        HttpResponse response = apiClient.logout();
        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_NO_CONTENT) {
            log.warn("Logout failed with status: " + response.getStatus());
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
