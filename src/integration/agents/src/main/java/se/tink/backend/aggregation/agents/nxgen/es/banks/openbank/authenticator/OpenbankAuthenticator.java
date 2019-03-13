package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

public class OpenbankAuthenticator implements Authenticator {
    private final OpenbankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public OpenbankAuthenticator(OpenbankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        String username = credentials.getField(Field.Key.USERNAME);
        String usernameType = credentials.getField(OpenbankConstants.USERNAME_TYPE);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        LoginRequest request =
                new LoginRequest.Builder()
                        .withUsername(username)
                        .withUsernameType(usernameType)
                        .withPassword(password)
                        .withForce(1)
                        .build();

        try {
            LoginResponse loginResponse = apiClient.login(request);

            if (!loginResponse.hasTokenCredential()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            putAuthTokenInSessionStorage(loginResponse);
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();

            if (response.getStatus() == HttpStatus.SC_BAD_REQUEST) {
                ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

                if (errorResponse.hasErrorCode(
                        OpenbankConstants.ErrorCodes.INCORRECT_CREDENTIALS)) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                }

                if (errorResponse.hasErrorCode(
                        OpenbankConstants.ErrorCodes.INVALID_LOGIN_USERNAME_TYPE)) {
                    throw new IllegalStateException(
                            String.format(
                                    "Invalid username type: %s",
                                    Optional.of(errorResponse.getErrorDescription()).orElse(null)));
                }
                // Fall through and re-throw original exception.
            }

            // Re-throw the exception.
            throw hre;
        }
    }

    private void putAuthTokenInSessionStorage(LoginResponse loginResponse) {
        loginResponse
                .getTokenCredential()
                .peek(authToken -> sessionStorage.put(OpenbankConstants.Storage.AUTH_TOKEN, authToken));
    }
}
