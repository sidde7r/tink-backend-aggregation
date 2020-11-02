package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankSessionHandler implements SessionHandler {
    private final SessionStorage sessionStorage;

    public DemobankSessionHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        sessionStorage.clear();
    }

    @Override
    public void keepAlive() throws SessionException {
        sessionStorage
                .get(DemobankConstants.StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                .filter(token -> !token.hasAccessExpired() || token.canRefresh())
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
