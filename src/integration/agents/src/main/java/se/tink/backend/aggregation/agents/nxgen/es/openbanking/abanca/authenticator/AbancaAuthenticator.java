package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class AbancaAuthenticator implements Authenticator {

    private final AbancaApiClient apiClient;

    public AbancaAuthenticator(AbancaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        apiClient.authenticate(credentials);
    }
}
