package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BunqSessionHandler implements SessionHandler {
    private final BunqApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BunqSessionHandler(BunqApiClient apiClient, SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
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
            apiClient.listAccounts(sessionStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
