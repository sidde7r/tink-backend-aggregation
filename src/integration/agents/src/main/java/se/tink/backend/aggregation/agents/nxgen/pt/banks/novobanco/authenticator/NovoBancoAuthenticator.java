package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator;

import static java.util.Objects.requireNonNull;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class NovoBancoAuthenticator implements PasswordAuthenticator {
    private final NovoBancoApiClient apiClient;

    public NovoBancoAuthenticator(final NovoBancoApiClient apiClient) {
        this.apiClient = requireNonNull(apiClient);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        apiClient.loginStep0(username, password);
    }
}
