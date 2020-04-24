package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankPasswordAuthenticator implements PasswordAuthenticator {
    private SessionStorage sessionStorage;
    public DemobankApiClient apiClient;

    public DemobankPasswordAuthenticator(
            SessionStorage sessionStorage, DemobankApiClient apiClient) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        sessionStorage.put("username", username);
        sessionStorage.put("password", password);

        TokenEntity tokenEntity = apiClient.login(username, password);
        sessionStorage.put("accessToken", tokenEntity.getToken());
        sessionStorage.put("refreshToken", tokenEntity.getRefreshToken());
        sessionStorage.put("expiresIn", tokenEntity.getExpiresIn());
    }
}
