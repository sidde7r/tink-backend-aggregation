package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.authenticator;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class CaixaPasswordAuthenticator implements PasswordAuthenticator {

    private final CaixaApiClient apiClient;

    public CaixaPasswordAuthenticator(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password) {
        apiClient.authenticate(username, password);
    }
}
