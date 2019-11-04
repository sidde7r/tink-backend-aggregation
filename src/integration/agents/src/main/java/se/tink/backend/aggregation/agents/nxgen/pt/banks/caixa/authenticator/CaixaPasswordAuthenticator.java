package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class CaixaPasswordAuthenticator implements PasswordAuthenticator {

    private final CaixaApiClient apiClient;

    public CaixaPasswordAuthenticator(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        apiClient.authenticate(username, password);
    }
}
