package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenWebApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class RaiffeisenSessionHandler implements SessionHandler {
    final private RaiffeisenWebApiClient apiClient;
    final private RaiffeisenSessionStorage raiffeisenSessionStorage;

    public RaiffeisenSessionHandler(final RaiffeisenWebApiClient apiClient,
            final RaiffeisenSessionStorage raiffeisenSessionStorage) {
        this.apiClient = apiClient;
        this.raiffeisenSessionStorage = raiffeisenSessionStorage;
    }

    @Override
    public void logout() {
        apiClient.logOut();
    }

    @Override
    public void keepAlive() throws SessionException {
        final WebLoginResponse loginResponse = raiffeisenSessionStorage.getWebLoginResponse().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        apiClient.keepAlive(loginResponse);
    }
}

