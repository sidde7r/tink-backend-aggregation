package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class N26PasswordAuthenticator implements PasswordAuthenticator {

    private final N26ApiClient n26ApiClient;
    private final SessionStorage sessionStorage;

    public N26PasswordAuthenticator(
            final N26ApiClient client, final SessionStorage sessionStorage) {
        this.n26ApiClient = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        String mfaToken = n26ApiClient.loginWithPassword(username, password);
        sessionStorage.put(N26Constants.Storage.MFA_TOKEN, mfaToken);
    }
}
