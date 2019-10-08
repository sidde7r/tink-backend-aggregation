package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.session;

import io.vavr.control.Try;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class OpenbankSessionHandler implements SessionHandler {
    private final OpenbankApiClient apiClient;

    public OpenbankSessionHandler(OpenbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        Try.of(apiClient::keepAlive).getOrElseThrow(e -> SessionError.SESSION_EXPIRED.exception(e));
    }
}
