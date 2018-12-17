package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.URL;

public class IngAtSessionHandler implements SessionHandler {
    final private IngAtApiClient apiClient;
    final private IngAtSessionStorage ingAtSessionStorage;

    public IngAtSessionHandler(final IngAtApiClient apiClient, final IngAtSessionStorage ingAtSessionStorage) {
        this.apiClient = apiClient;
        this.ingAtSessionStorage = ingAtSessionStorage;
    }

    @Override
    public void logout() {
        ingAtSessionStorage.clear();
        apiClient.logOut();
    }

    @Override
    public void keepAlive() throws SessionException {
        final WebLoginResponse webLoginResponse = ingAtSessionStorage.getWebLoginResponse().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        apiClient.keepAlive();
    }
}