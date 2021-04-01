package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.AuthenticationStates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class BbvaAuthenticator implements MultiFactorAuthenticator {
    private final BbvaApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final CredentialsRequest request;

    public BbvaAuthenticator(
            BbvaApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            CredentialsRequest request) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.request = request;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final UserCredentials userCredentials = new UserCredentials(credentials);
        final LoginRequest loginRequest = new LoginRequest(userCredentials);
        try {
            LoginResponse loginResponse = apiClient.login(loginRequest);
            String authenticationState = loginResponse.getAuthenticationState();
            log.info("Authentication state: {}", authenticationState);
            if (isTwoFactorAuthNeeded(authenticationState)) {
                abortIfUserNotAvailableForInteraction();
                loginWithOtp(loginResponse.getMultistepProcessId(), userCredentials);
            }
        } catch (HttpResponseException ex) {
            mapHttpErrors(ex);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void loginWithOtp(String multistepProcessId, UserCredentials userCredentials) {
        log.info("Process Otp has been started");
        final LoginRequest loginOtpRequest = new LoginRequest(userCredentials, multistepProcessId);
        LoginResponse loginOtpResponse = apiClient.login(loginOtpRequest);
        String otpCode = supplementalInformationHelper.waitForOtpInput();
        final LoginRequest otpRequest =
                new LoginRequest(
                        userCredentials, otpCode, loginOtpResponse.getMultistepProcessId());
        apiClient.login(otpRequest);
    }

    private boolean isTwoFactorAuthNeeded(String authenticationState) {
        return authenticationState.equalsIgnoreCase(AuthenticationStates.GO_ON);
    }

    private void abortIfUserNotAvailableForInteraction() {
        if (userNotAvailableForInteraction()) {
            throw BankServiceError.SESSION_TERMINATED.exception(
                    "SCA request during auto refresh, aborting authentication");
        }
    }

    private boolean userNotAvailableForInteraction() {
        return !request.getUserAvailability().isUserAvailableForInteraction();
    }

    private void mapHttpErrors(HttpResponseException e) throws LoginException {
        HttpResponse response = e.getResponse();
        if (response.getStatus() >= 400) {
            BbvaErrorResponse errorResponse = e.getResponse().getBody(BbvaErrorResponse.class);
            if (HttpStatus.SC_INTERNAL_SERVER_ERROR <= errorResponse.getHttpStatus()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            switch (errorResponse.getHttpStatus()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    throw AuthorizationError.UNAUTHORIZED.exception();
                case HttpStatus.SC_CONFLICT:
                    throw LoginError.NOT_CUSTOMER.exception();
                case HttpStatus.SC_FORBIDDEN:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                default:
                    String message =
                            String.format(
                                    "Unknown error: httpStatus %s, code %s, message %s",
                                    errorResponse.getHttpStatus(),
                                    errorResponse.getErrorCode(),
                                    errorResponse.getErrorMessage());
                    throw LoginError.DEFAULT_MESSAGE.exception(message);
            }
        }
        throw e;
    }
}
