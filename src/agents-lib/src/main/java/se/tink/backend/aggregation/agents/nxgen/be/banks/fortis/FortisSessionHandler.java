package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FortisSessionHandler implements SessionHandler {
    private final FortisApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public FortisSessionHandler(FortisApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {
    }

    private boolean calulcatedChallengeExists() {
        return persistentStorage.containsKey(FortisConstants.STORAGE.CALCULATED_CHALLENGE);
    }

    private void tryFetchAccounts() throws SessionException {
        try {
            this.apiClient.fetchAccounts();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        if (calulcatedChallengeExists()) {
            tryFetchAccounts();
        } else {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
