package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class N26PasswordAuthenticator implements PasswordAuthenticator {

    private final N26ApiClient n26ApiClient;

    public N26PasswordAuthenticator(N26ApiClient client) {
        this.n26ApiClient = client;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        n26ApiClient.login(username, password);
    }
}
