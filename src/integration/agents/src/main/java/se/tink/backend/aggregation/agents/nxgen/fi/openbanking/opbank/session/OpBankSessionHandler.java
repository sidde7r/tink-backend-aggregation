package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class OpBankSessionHandler implements SessionHandler {

    private final OpBankApiClient apiClient;

    public OpBankSessionHandler(OpBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        throw new SessionException(SessionError.SESSION_EXPIRED);
    }
}
