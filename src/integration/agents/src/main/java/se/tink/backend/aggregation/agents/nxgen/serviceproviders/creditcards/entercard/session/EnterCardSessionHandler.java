package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@RequiredArgsConstructor
public class EnterCardSessionHandler implements SessionHandler {
    private final EnterCardApiClient apiClient;

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            // The user details endpoint is used for both credit card and identity data fetching.
            // These are the only two capabilities the EnterCard agents support so from a compliance
            // perspective this request is OK.
            apiClient.fetchUserDetails();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
