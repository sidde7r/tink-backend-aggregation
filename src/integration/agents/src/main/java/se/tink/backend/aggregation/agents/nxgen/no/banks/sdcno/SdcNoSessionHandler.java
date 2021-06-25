package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class SdcNoSessionHandler implements SessionHandler {
    private final SdcNoApiClient apiClient;

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchAgreement();
        } catch (HttpResponseException e) {
            log.error("[SDC] Caught exception while checking if session is active", e);
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
