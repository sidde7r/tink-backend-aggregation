package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.AuthenticationStates;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class BbvaAuthenticator implements MultiFactorAuthenticator {
    private final BbvaApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private CredentialsRequest request;

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
        String username = BbvaUtils.formatUsername(credentials.getField(CredentialKeys.USERNAME));
        String password = credentials.getField(CredentialKeys.PASSWORD);
        final LoginRequest loginRequest = new LoginRequest(username, password, null, false);
        try {
            LoginResponse loginResponse = apiClient.login(loginRequest);
            String authenticationState = loginResponse.getAuthenticationState();
            log.info("Authentication state: {}", authenticationState);
            if (isTwoFactorAuthenticationNeeded(authenticationState)) {
                processOtpLogin(loginResponse.getMultistepProcessId(), username);
            }
        } catch (HttpResponseException ex) {
            mapHttpErrors(ex);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void processOtpLogin(String multistepProcessId, String username) {
        log.info("Process Otp has been started");
        final LoginRequest loginOtpRequest = new LoginRequest(username, multistepProcessId);
        LoginResponse loginOtpResponse = apiClient.login(loginOtpRequest);
        String otpCode = supplementalInformationHelper.waitForOtpInput();
        String multistepId =
                StringUtils.firstNonEmpty(
                        loginOtpResponse.getMultistepProcessId(), multistepProcessId);
        final LoginRequest otpRequest = new LoginRequest(username, otpCode, multistepId, true);
        log.info("Otp code has been sent to the verification");
        apiClient.sendOTP(otpRequest);
    }

    private boolean isTwoFactorAuthenticationNeeded(String authenticationState) {
        if (!request.isManual()
                && authenticationState.equalsIgnoreCase(AuthenticationStates.GO_ON)) {
            throw BankServiceError.SESSION_TERMINATED.exception(
                    "SCA request during auto refresh, aborting authentication");
        }
        return authenticationState.equalsIgnoreCase(AuthenticationStates.GO_ON)
                && request.isManual();
    }

    private void mapHttpErrors(HttpResponseException e) throws LoginException {
        BbvaErrorResponse errorResponse = e.getResponse().getBody(BbvaErrorResponse.class);
        if (errorResponse.isIncorrectCredentials()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        if (errorResponse.isInternalServerError()) {
            throw LoginError.NOT_SUPPORTED.exception();
        }
        log.info(
                "Unknown error: httpStatus {}, code {}, message {}",
                errorResponse.getHttpStatus(),
                errorResponse.getErrorCode(),
                errorResponse.getErrorMessage());
        throw LoginError.DEFAULT_MESSAGE.exception();
    }
}
