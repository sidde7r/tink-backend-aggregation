package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class BbvaSessionHandler implements SessionHandler {

    private final BbvaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BbvaSessionHandler(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(sessionStorage.get(StorageKeys.TOKEN)))
            throw SessionError.SESSION_EXPIRED.exception();
    }
}
