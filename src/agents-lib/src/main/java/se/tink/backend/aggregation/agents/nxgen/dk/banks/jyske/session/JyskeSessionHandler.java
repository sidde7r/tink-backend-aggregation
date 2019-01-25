package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class JyskeSessionHandler implements SessionHandler {

    private JyskeApiClient apiClient;
    private Credentials credentials;

    public JyskeSessionHandler(JyskeApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public void logout() {
        apiClient.logout(credentials.getField(Field.Key.USERNAME));
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
