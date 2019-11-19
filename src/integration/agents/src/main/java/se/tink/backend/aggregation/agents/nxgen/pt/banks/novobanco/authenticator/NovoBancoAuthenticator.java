package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.AUTH_COOKIE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.DEVICE_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.SESSION_COOKIE_KEY;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NovoBancoAuthenticator implements PasswordAuthenticator {
    private final NovoBancoApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NovoBancoAuthenticator(
            final NovoBancoApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = requireNonNull(apiClient);
        this.sessionStorage = requireNonNull(sessionStorage);
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        Login0Response response = apiClient.loginStep0(username, password);

        if (!response.isValidCredentials()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        sessionStorage.put(AUTH_COOKIE_KEY, response.getBody().getSession().getAuthCookie());
        sessionStorage.put(SESSION_COOKIE_KEY, response.getBody().getSession().getSessionCookie());
        sessionStorage.put(DEVICE_ID_KEY, response.getBody().getDevice().getId());
    }
}
