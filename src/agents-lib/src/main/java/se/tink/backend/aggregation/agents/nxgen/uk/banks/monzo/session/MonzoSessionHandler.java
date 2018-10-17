package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo.MonzoConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoSessionHandler implements SessionHandler {

    private final MonzoApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public MonzoSessionHandler(MonzoApiClient client, PersistentStorage storage) {
        apiClient = client;
        persistentStorage = storage;
    }

    @Override
    public void logout() {
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!persistentStorage.containsKey(MonzoConstants.StorageKey.OAUTH_TOKEN) || !apiClient.ping().isAuthenticated()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

}
