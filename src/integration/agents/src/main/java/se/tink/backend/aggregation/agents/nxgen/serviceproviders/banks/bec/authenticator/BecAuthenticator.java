package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessage;
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

        try {
            apiClient.appSync();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 400) {
                LoginErrorResponse response = e.getResponse().getBody(LoginErrorResponse.class);
                String errorMessage = response.getMessage().toLowerCase();
                if (errorMessage.contains(ErrorMessage.FUNCTION_NOT_AVAILABLE_DANISH)) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                } else if (errorMessage.contains(ErrorMessage.NETBANK_REQUIRED)
                        || errorMessage.contains(ErrorMessage.NETBANK_REQUIRED_DANISH)) {
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
                }
            }
            throw e;
        }

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
                } else if (errorMessage.contains(ErrorMessage.USER_LOCKED)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                } else if (errorMessage.contains(ErrorMessage.NETBANK_REQUIRED)
                        || errorMessage.contains(ErrorMessage.NETBANK_REQUIRED_DANISH)) {
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
                }
            }
            throw e;
        }
    }
}
