package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LansforsakringarAuthenticator implements Authenticator {

    private final LansforsakringarApiClient apiClient;
    private final SessionStorage sessionStorage;

    public LansforsakringarAuthenticator(
            LansforsakringarApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final OAuth2Token token = apiClient.authenticate();

        sessionStorage.put(StorageKeys.ACCESS_TOKEN, token);
    }
}
