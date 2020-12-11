package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DemobankSessionHandler implements SessionHandler {
    private final PersistentStorage persistentStorage;

    public DemobankSessionHandler(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {
        persistentStorage.clear();
    }

    @Override
    public void keepAlive() throws SessionException {
        persistentStorage
                .get(DemobankConstants.StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                .filter(token -> !token.hasAccessExpired())
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
