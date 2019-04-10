package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.session;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class BecSessionHandler implements SessionHandler {
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecSessionHandler(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        // TODO: Will changed for production, because tokens are not supported right now
        if (Strings.isNullOrEmpty(sessionStorage.get(BecConstants.StorageKeys.CLIENT_ID))) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
