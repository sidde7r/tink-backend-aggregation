package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class NordeaV21Authenticator<T extends NordeaV21ApiClient> implements PasswordAuthenticator {
    protected final T client;

    public NordeaV21Authenticator(T client) {
        this.client = client;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        String token = client.passwordLogin(username, password).getToken()
                .orElseThrow(() -> new IllegalStateException("No token present on the lightLoginResponse"));
        client.setToken(token);
    }
}
