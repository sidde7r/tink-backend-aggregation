package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.AuthenticationKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.EnrollmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.rpc.CajamarErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class CajamarAuthenticator implements MultiFactorAuthenticator {

    private final CajamarApiClient apiClient;

    public CajamarAuthenticator(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        EnrollmentRequest request = new EnrollmentRequest(credentials);
        try {
            apiClient.fetchEnrollment(request);
            LoginRequest loginRequest =
                    new LoginRequest(credentials.getField(AuthenticationKeys.PASSWORD));
            apiClient.login(loginRequest);
        } catch (HttpResponseException e) {
            mapHttpErrors(e);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void mapHttpErrors(HttpResponseException e) {
        HttpResponse response = e.getResponse();
        if (response.getStatus() >= 400) {
            if (HttpStatus.SC_INTERNAL_SERVER_ERROR <= response.getStatus()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            CajamarErrorResponse errorResponse = response.getBody(CajamarErrorResponse.class);

            switch (response.getStatus()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    throw AuthorizationError.UNAUTHORIZED.exception();
                case HttpStatus.SC_FORBIDDEN:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                default:
                    String message =
                            String.format(
                                    "Error message: httpStatus: %s, code: %s, message: %s",
                                    response.getStatus(),
                                    errorResponse.getCode(),
                                    errorResponse.getMessage());
                    throw LoginError.DEFAULT_MESSAGE.exception(message);
            }
        }
        throw e;
    }
}
