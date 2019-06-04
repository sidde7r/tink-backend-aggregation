package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class UnicreditAuthenticator implements Authenticator {

    private final UnicreditApiClient apiClient;

    public UnicreditAuthenticator(UnicreditApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        apiClient.authenticate();
    }
}
