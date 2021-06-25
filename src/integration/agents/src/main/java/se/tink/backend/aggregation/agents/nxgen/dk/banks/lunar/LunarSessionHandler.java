package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class LunarSessionHandler implements SessionHandler {
    private final AuthenticationApiClient apiClient;
    private final LunarDataAccessorFactory accessorFactory;
    private final PersistentStorage persistentStorage;

    @Override
    public void logout() {
        // nop
    }

    @Override
    public void keepAlive() throws SessionException {
        LunarAuthData authData = getLunarPersistedData();
        if (authData.getAccessToken() == null || authData.getDeviceId() == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            apiClient.fetchAccounts(authData.getAccessToken(), authData.getDeviceId());
        } catch (HttpResponseException e) {
            log.error("Caught exception while checking if session is active", e);
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private LunarAuthData getLunarPersistedData() {
        return accessorFactory
                .createAuthDataAccessor(
                        new PersistentStorageService(persistentStorage)
                                .readFromAgentPersistentStorage())
                .get();
    }
}
