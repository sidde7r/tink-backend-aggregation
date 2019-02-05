package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Credentials;

public class AktiaSessionHandler implements SessionHandler {
    private final AktiaApiClient apiClient;
    private final Credentials credentials;

    private AktiaSessionHandler(AktiaApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public static AktiaSessionHandler createFromApiClientAndCredentials(AktiaApiClient apiClient,
                                                                        Credentials credentials) {
        return new AktiaSessionHandler(apiClient, credentials);
    }

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.accountsSummary();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
