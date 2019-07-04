package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngSandboxAuthenticator implements Authenticator {

    private final IngApiClient client;
    private final SessionStorage sessionStorage;

    public IngSandboxAuthenticator(IngApiClient apiClient, SessionStorage sessionStorage) {
        this.client = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        client.authenticate();
        client.setTokenToSession(
                client.getToken(
                        "76175013-94d0-411d-927c-0af6bb828c7c")); // Static token from
                                                                  // https://developer.ing.com/static/AIS.pdf
    }
}
