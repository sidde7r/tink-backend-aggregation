package se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.CoopConstants;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.entities.AuthenticateResultEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.coop.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

public class CoopPasswordAuthenticator implements PasswordAuthenticator {

    private final CoopApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public CoopPasswordAuthenticator(CoopApiClient apiClient, SessionStorage sessionStorage, Credentials credentials) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {

        AuthenticateResponse authenticateResponse = apiClient.authenticate(username, password);

        if (authenticateResponse == null && authenticateResponse.getAuthenticateResult() == null) {
            throw new IllegalStateException("No response data found");
        }
        AuthenticateResultEntity authResult = authenticateResponse.getAuthenticateResult();
        String token = authResult.getToken();

        if (Strings.isNullOrEmpty(token) || authResult.getUserId() <= 0) {
            throw new IllegalStateException("No auth data found");
        }

        sessionStorage.put(CoopConstants.Storage.USER_ID ,String.valueOf(authResult.getUserId()));
        sessionStorage.put(CoopConstants.Storage.TOKEN ,token);
        sessionStorage.put(CoopConstants.Storage.USER_SUMMARY, authResult.getUserSummary());
        sessionStorage.put(CoopConstants.Storage.CREDENTIALS_ID, credentials.getId());
    }
}
