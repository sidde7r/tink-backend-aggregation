package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

// TODO: Authentication must be implemented for the production
public class BecAuthenticator implements Authenticator {
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecAuthenticator(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {}
}
