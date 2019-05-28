package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BunqSessionHandler implements SessionHandler {
    private final BunqApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;

    public BunqSessionHandler(
            BunqApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TemporaryStorage temporaryStorage) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!sessionStorage.containsKey(BunqBaseConstants.StorageKeys.USER_ID)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            // We need to update the client authentication token before we can make a call toward
            // their API or we won't have the correct headers
            BunqAuthenticator.updateClientAuthToken(
                    sessionStorage, persistentStorage, temporaryStorage);
            apiClient.listAccounts(sessionStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
