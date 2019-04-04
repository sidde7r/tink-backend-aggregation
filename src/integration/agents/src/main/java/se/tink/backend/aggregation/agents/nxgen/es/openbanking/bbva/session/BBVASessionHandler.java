package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class BBVASessionHandler implements SessionHandler {
    private final BBVAApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BBVASessionHandler(BBVAApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(BBVAConstants.StorageKeys.TOKEN)))
            throw SessionError.SESSION_EXPIRED.exception();
    }
}
