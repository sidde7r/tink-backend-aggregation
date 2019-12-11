package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class JyskeSessionHandler implements SessionHandler {

    private final JyskeApiClient apiClient;
    private final Credentials credentials;
    private final JyskePersistentStorage jyskePersistentStorage;

    public JyskeSessionHandler(
            JyskeApiClient apiClient,
            Credentials credentials,
            JyskePersistentStorage jyskePersistentStorage) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.jyskePersistentStorage = jyskePersistentStorage;
    }

    @Override
    public void logout() {
        apiClient.logout(credentials.getField(Field.Key.USERNAME));
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (!jyskePersistentStorage.containsInstallId()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            apiClient.fetchAccounts();

        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
