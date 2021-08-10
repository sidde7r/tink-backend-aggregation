package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqClientAuthTokenHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class BunqSessionHandler implements SessionHandler {
    private final BunqApiClient apiClient;
    private final BunqClientAuthTokenHandler clientAuthTokenHandler;
    private final SessionStorage sessionStorage;

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
            clientAuthTokenHandler.updateClientAuthToken(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);
            apiClient.listAccounts(sessionStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
