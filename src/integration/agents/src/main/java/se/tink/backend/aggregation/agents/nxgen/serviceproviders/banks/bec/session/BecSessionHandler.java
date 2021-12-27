package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.Log.BEC_LOG_TAG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class BecSessionHandler implements SessionHandler {

    private final BecApiClient apiClient;

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchAccounts();
            log.info("{} Session still valid", BEC_LOG_TAG);

        } catch (HttpResponseException e) {

            if (e.getResponse().getStatus() == 401 && !e.getResponse().hasBody()) {
                log.info("{} Session expired", BEC_LOG_TAG);
                throw SessionError.SESSION_EXPIRED.exception();
            }

            log.error("Unknown exception while checking if session is active", e);
            throw e;
        }
    }
}
