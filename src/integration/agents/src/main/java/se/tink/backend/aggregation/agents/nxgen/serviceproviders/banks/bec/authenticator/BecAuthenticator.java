package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.LoginErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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
            apiClient.scaPrepare(username, password);
            String token =
                    apiClient.scaPrepare2(username, password).getCodeappTokenDetails().getToken();
            apiClient.pollNemId(token);
            apiClient.sca(username, password, token);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 400) {
                LoginErrorResponse response = e.getResponse().getBody(LoginErrorResponse.class);
                String errorMessage = response.getMessage().toLowerCase();
                if (errorMessage.contains(ErrorMessage.FUNCTION_NOT_AVAILABLE_DANISH)) {
                    throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
                } else if (errorMessage.contains(ErrorMessage.NETBANK_REQUIRED)
                        || errorMessage.contains(ErrorMessage.NETBANK_REQUIRED_DANISH)) {
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception(e);
                } else if (errorMessage.contains(ErrorMessage.USER_LOCKED)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(e);
                } else if (errorMessage.contains(BecConstants.ErrorMessage.PIN_LOCKED)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(e);
                } else if (errorMessage.contains(BecConstants.ErrorMessage.INVALID_CREDENTIAL)) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                }
            }
            throw e;
        }
    }
}
