package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import io.vavr.control.Option;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class BbvaAuthenticator implements MultiFactorAuthenticator {
    private final BbvaApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final CredentialsRequest request;
    private final TransactionPaginationHelper transactionPaginationHelper;

    private List<TransactionalAccount> accounts = Collections.emptyList();

    public BbvaAuthenticator(
            BbvaApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            CredentialsRequest request,
            TransactionPaginationHelper transactionPaginationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.request = request;
        this.transactionPaginationHelper = transactionPaginationHelper;
    }

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
                if (request.getUserAvailability().isUserAvailableForInteraction()
                        && isInExtendedPeriod()) {
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
        AtomicBoolean inExtendedPeriod = new AtomicBoolean(false);
        accounts = getAccounts();
        apiClient.fetchUpdateTransactions(accounts);
        List<TransactionalAccount> extendedPeriodAccounts =
                accounts.stream()
                        .filter(
                                theAccount -> {
                                    Optional<Date> dateLimit =
                                            transactionPaginationHelper.getTransactionDateLimit(
                                                    theAccount);
                                    return !dateLimit.isPresent()
                                            || BbvaUtils.isMoreThan90DaysOld(dateLimit.get());
                                })
                        .collect(Collectors.toList());
        // one account is enough to set
        extendedPeriodAccounts.stream().findFirst().ifPresent(it -> inExtendedPeriod.set(true));
        return inExtendedPeriod.get();
    }

    public List<TransactionalAccount> getAccounts() {
        FinancialDashboardResponse dashboardResponse = apiClient.fetchFinancialDashboard();
        return dashboardResponse
                .getPositions()
                .map(PositionEntity::getContract)
                .map(ContractEntity::getAccount)
                .filter(Option::isDefined)
                .map(Option::get)
                .filter(AccountEntity::isTransactionalAccount)
                .filter(AccountEntity::hasBalance)
                .map(
                        accountEntity ->
                                accountEntity.toTinkTransactionalAccount(Collections.emptyList()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void forcedOtpForExtendedPeriod() {
        try {
            apiClient.requestMoreThan90DaysTransactionsForFirstAccount(accounts);
        } catch (HttpResponseException e) {
            mapHttpErrors(e, true);
        }
    }
}
