package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@RequiredArgsConstructor
public class DnbSessionHandler implements SessionHandler {

    private final DnbApiClient apiClient;

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        /*
        - if session is still valid, we should be able to fetch accounts
        - if session is not valid, DNB sometimes returns HTML page with status 200, other times it's status 302
        - if API is temporarily down, we handle it already with retry filters
        - if API is down or we're making invalid request - it will still be logged later on during
          authentication / data fetching
         */
        try {
            apiClient.fetchAccounts();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
