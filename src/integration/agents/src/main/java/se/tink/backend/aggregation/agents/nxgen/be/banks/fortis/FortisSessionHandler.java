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
        apiClient.logout();
    }

    private boolean calulcatedChallengeExists() {
        return persistentStorage.containsKey(FortisConstants.STORAGE.CALCULATED_CHALLENGE);
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (calulcatedChallengeExists()) {
                this.apiClient.fetchAccounts();
            } else {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
