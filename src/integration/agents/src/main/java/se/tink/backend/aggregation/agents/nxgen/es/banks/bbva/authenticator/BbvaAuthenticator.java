package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.TransactionsFetchingDateFromManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
@RequiredArgsConstructor
public class BbvaAuthenticator implements MultiFactorAuthenticator {
    private final BbvaApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final CredentialsRequest request;
    private final TransactionsFetchingDateFromManager transactionsFetchingDateFromManager;

    private List<TransactionalAccount> accounts = Collections.emptyList();

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final UserCredentials userCredentials = new UserCredentials(credentials);
        if (userCredentials.getUsername() != null) {
            final LoginRequest loginRequest = new LoginRequest(userCredentials);
            try {
                LoginResponse loginResponse = apiClient.login(loginRequest);
                String authenticationState = loginResponse.getAuthenticationState();
                log.info("Authentication state: {}", authenticationState);
                if (isTwoFactorAuthNeeded(authenticationState)) {
                    abortIfUserNotAvailableForInteraction();
                    loginWithOtp(loginResponse.getMultistepProcessId(), userCredentials);
                }
                transactionsFetchingDateFromManager.init();
                if (isInExtendedPeriod()) {
                    abortIfUserNotAvailableForInteraction();
                    forcedOtpForExtendedPeriod();
                }
            } catch (HttpResponseException ex) {
                mapHttpErrors(ex);
            }
        } else {
            throw LoginError.INCORRECT_CREDENTIALS.exception("Username with invalid format");
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
        return AuthenticationStates.GO_ON.equalsIgnoreCase(authenticationState);
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
        mapHttpErrors(e, false);
    }

    private void mapHttpErrors(HttpResponseException e, boolean otpMode) throws LoginException {
        HttpResponse response = e.getResponse();
        if (response.getStatus() >= 400) {
            BbvaErrorResponse errorResponse = e.getResponse().getBody(BbvaErrorResponse.class);
            if (HttpStatus.SC_INTERNAL_SERVER_ERROR <= errorResponse.getHttpStatus()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            if (otpMode) {
                processOtpError(errorResponse);
            } else {
                processFirstLoginError(errorResponse);
            }
        }
        throw e;
    }

    private void processOtpError(BbvaErrorResponse errorResponse) {
        if (errorResponse.getHttpStatus() == HttpStatus.SC_UNAUTHORIZED) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }
        String message =
                String.format(
                        "Unknown error: httpStatus %s, code %s, message %s",
                        errorResponse.getHttpStatus(),
                        errorResponse.getErrorCode(),
                        errorResponse.getErrorMessage());
        throw LoginError.DEFAULT_MESSAGE.exception(message);
    }

    private void processFirstLoginError(BbvaErrorResponse errorResponse) {
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

    public boolean isInExtendedPeriod() {
        LocalDate regularConsentMaxAllowedDateFrom = LocalDate.now().minusDays(89);
        return transactionsFetchingDateFromManager
                .getComputedDateFrom()
                .map(d -> d.isBefore(regularConsentMaxAllowedDateFrom))
                .orElse(true);
    }

    public void forcedOtpForExtendedPeriod() {
        try {
            apiClient.requestMoreThan90DaysTransactionsForFirstAccount(accounts);
        } catch (HttpResponseException e) {
            mapHttpErrors(e, true);
        }
    }
}
