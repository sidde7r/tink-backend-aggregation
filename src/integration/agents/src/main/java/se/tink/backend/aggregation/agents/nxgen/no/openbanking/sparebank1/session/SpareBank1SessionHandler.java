package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SpareBank1SessionHandler implements SessionHandler {

    private final SpareBank1ApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SpareBank1SessionHandler(
            SpareBank1ApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(persistentStorage.get(StorageKeys.OAUTH_TOKEN))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
