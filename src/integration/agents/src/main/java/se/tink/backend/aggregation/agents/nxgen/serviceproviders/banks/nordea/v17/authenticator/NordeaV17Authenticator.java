package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class NordeaV17Authenticator<T extends NordeaV17ApiClient> implements PasswordAuthenticator {
    protected final T client;

    public NordeaV17Authenticator(T client) {
        this.client = client;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        String token =
                client.passwordLogin(username, password)
                        .getToken()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No token present on the lightLoginResponse"));
        client.setToken(token);
    }
}
