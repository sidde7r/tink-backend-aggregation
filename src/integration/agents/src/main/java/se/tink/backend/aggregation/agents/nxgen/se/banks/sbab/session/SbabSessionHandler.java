package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session;

import io.vavr.control.Try;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SbabSessionHandler implements SessionHandler {
    private final SbabApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SbabSessionHandler(SbabApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        Try.of(apiClient::listAccounts)
                .getOrElseThrow(() -> new SessionException(SessionError.SESSION_EXPIRED));
    }
}
