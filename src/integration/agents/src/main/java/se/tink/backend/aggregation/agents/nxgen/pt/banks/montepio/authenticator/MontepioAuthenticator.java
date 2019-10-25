package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator;

import static java.util.Objects.requireNonNull;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class MontepioAuthenticator implements PasswordAuthenticator {

    private final MontepioApiClient client;

    public MontepioAuthenticator(final MontepioApiClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        client.loginStep0(username, password);
        client.loginStep1();
    }
}
