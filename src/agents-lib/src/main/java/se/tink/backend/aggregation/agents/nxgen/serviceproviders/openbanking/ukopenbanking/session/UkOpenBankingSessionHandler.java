package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class UkOpenBankingSessionHandler implements SessionHandler {
    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        // Session is handled by OpenIdAuthenticationController.autoAuthenticate().
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
