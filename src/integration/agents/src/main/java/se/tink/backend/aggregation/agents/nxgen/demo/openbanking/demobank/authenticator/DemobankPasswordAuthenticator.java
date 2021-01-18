package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class DemobankPasswordAuthenticator implements PasswordAuthenticator {
    public DemobankApiClient apiClient;

    public DemobankPasswordAuthenticator(DemobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        try {
            OAuth2Token oAuth2Token = apiClient.login(username, password);
            apiClient.setTokenToStorage(oAuth2Token);
        } catch (HttpResponseException e) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(e);
        }
    }
}
