package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenWebApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import tink.org.apache.http.HttpStatus;

public class RaiffeisenSessionHandler implements SessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RaiffeisenSessionHandler.class);
    final private RaiffeisenWebApiClient apiClient;
    final private RaiffeisenSessionStorage raiffeisenSessionStorage;

    public RaiffeisenSessionHandler(final RaiffeisenWebApiClient apiClient,
            final RaiffeisenSessionStorage raiffeisenSessionStorage) {
        this.apiClient = apiClient;
        this.raiffeisenSessionStorage = raiffeisenSessionStorage;
    }

    @Override
    public void logout() {
        raiffeisenSessionStorage.clear();
        apiClient.logOut();
    }

    @Override
    public void keepAlive() throws SessionException {
        final WebLoginResponse loginResponse = raiffeisenSessionStorage.getWebLoginResponse()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        try {
            apiClient.keepAlive(loginResponse);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() != HttpStatus.SC_UNAUTHORIZED) {
                logger.warn("Unexpected HTTP Status code {}:{}", e.getResponse().getStatus(), e);
            }
            logout();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}

