package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BecAuthenticator implements PasswordAuthenticator {

    private final BecApiClient apiClient;

    public BecAuthenticator(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        apiClient.appSync();

        try {
            apiClient.logonChallenge(username, password);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 400) {
                LoginErrorResponse response = e.getResponse().getBody(LoginErrorResponse.class);
                String errorMessage = response.getMessage().toLowerCase();
                if (errorMessage.contains(BecConstants.ErrorMessage.INVALID_CREDENTIAL)) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                } else if (errorMessage.contains(BecConstants.ErrorMessage.PIN_LOCKED)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                }
            }
            throw e;
        }
    }
}
