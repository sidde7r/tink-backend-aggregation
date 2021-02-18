package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session;

import io.vavr.control.Try;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BbvaSessionHandler implements SessionHandler {
    private final BbvaApiClient apiClient;

    public BbvaSessionHandler(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        Try.of(apiClient::isAlive)
                .getOrElseThrow(
                        (Supplier<SessionException>) SessionError.SESSION_EXPIRED::exception);
    }
}
