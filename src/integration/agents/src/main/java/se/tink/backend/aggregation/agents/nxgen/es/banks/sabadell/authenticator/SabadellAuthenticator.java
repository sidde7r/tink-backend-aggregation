package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Constants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SabadellAuthenticator implements PasswordAuthenticator {
    private final SabadellApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SabadellAuthenticator(SabadellApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            SessionResponse response = apiClient.initiateSession(username, password);
            sessionStorage.put(Constants.SESSION_KEY, response);
        } catch (HttpResponseException e) {
            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            String errorCode = response.getErrorCode();

            if (SabadellConstants.ErrorCodes.INCORRECT_CREDENTIALS.equalsIgnoreCase(errorCode)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }

            throw new IllegalStateException(
                    String.format(
                            "%s: Login failed with error code: %s, error message: %s",
                            SabadellConstants.Tags.LOGIN_ERROR,
                            response.getErrorCode(),
                            response.getErrorMessage()));
        }
    }
}
