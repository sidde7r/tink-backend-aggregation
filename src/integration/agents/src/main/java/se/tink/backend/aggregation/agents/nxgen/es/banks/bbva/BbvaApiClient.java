package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Body.EMPTY_BODY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Dates.CARD_STAMP_DATE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Dates.END_OF_DAY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Dates.OPENING_DATE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Dates.START_OF_DAY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Dates.YYYY_MM_DD_FORMAT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.ExtendedPeriod.OTP_SMS_SUFFIX;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers.RETRY_ATTEMPTS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers.TIMEOUT_RETRY_SLEEP_MILLISECONDS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter.SHOW_CONTRACT_TRANSACTIONS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter.SORTED_BY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter.SORTED_TYPE;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ParticipantsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.SearchFiltersEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.TransactionDateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AmountFilterEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.DateFilterEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FilterEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.UpdateTransactionsContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.UpdateTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.filter.BbvaInvestmentAccountBlockedFilter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.TransactionsFetchingDateFromManager;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class BbvaApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    @Setter private TransactionsFetchingDateFromManager transactionsFetchingDateFromManager;

    public BbvaApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        client.addFilter(new BbvaInvestmentAccountBlockedFilter());
    }

    public HttpResponse isAlive() {
        try {
            return createPostRequestInSession(Url.REFRESH_TICKET)
                    .queryParam(QueryKeys.ISALIVE_CUSTOMER_ID, getUserId())
                    .post(HttpResponse.class, EMPTY_BODY);
        } catch (HttpResponseException httpResponseException) {
            if (isKeepAliveResponseOk(httpResponseException)) {
                return httpResponseException.getResponse();
            }
            throw httpResponseException;
        }
    }

    private boolean isKeepAliveResponseOk(HttpResponseException httpResponseException) {
        return httpResponseException != null
                && httpResponseException.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        HttpResponse httpResponse = createLoginRequest().post(HttpResponse.class, loginRequest);
        LoginResponse loginResponse = httpResponse.getBody(LoginResponse.class);
        setTsec(httpResponse.getHeaders().getFirst(HeaderKeys.TSEC_KEY));
        setUserId(loginResponse.getUser().getId());
        return loginResponse;
    }

    private RequestBuilder createLoginRequest() {
        return createPostRequest(BbvaConstants.Url.TICKET).header(Headers.CHROME_UA);
    }

    public void logout() {
        // nothing to do
    }

    public FinancialDashboardResponse fetchFinancialDashboard() {
        return createGetRequestInSession(BbvaConstants.Url.FINANCIAL_DASHBOARD)
                .queryParam(QueryKeys.DASHBOARD_CUSTOMER_ID, getUserId())
                .queryParam(QueryKeys.DASHBOARD_FILTER, QueryValues.DASHBOARD_FILTER)
                .get(FinancialDashboardResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransactionsToForceOtp(
            Account account, String pageKey) {
        return fetchAccountTransactions(account, pageKey, true);
    }

    public AccountTransactionsResponse fetchAccountTransactions(Account account, String pageKey) {
        return fetchAccountTransactions(account, pageKey, false);
    }

    public AccountTransactionsResponse fetchAccountTransactions(
            Account account, String pageKey, boolean otpRequest) {
        TransactionsRequest body = createAccountTransactionsRequestBody(account);
        RequestBuilder request = buildAccountTransactionRequest(pageKey, otpRequest);
        try {
            return request.post(AccountTransactionsResponse.class, body);
        } catch (HttpResponseException e) {
            HttpResponse exceptionResponse = e.getResponse();
            BbvaErrorResponse errorResponse = exceptionResponse.getBody(BbvaErrorResponse.class);
            if (isSecondFactorAuthenticationNeeded(errorResponse)) {
                return fetchAccountTransactionsWithOtp(exceptionResponse, body);
            }
            if (errorResponse.isConflictStatus() || errorResponse.isInternalServerError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            log.info(
                    "Unknown error: httpStatus {}, code {}, message {} otpRequest {}",
                    errorResponse.getHttpStatus(),
                    errorResponse.getErrorCode(),
                    errorResponse.getErrorMessage(),
                    otpRequest);
            throw e;
        }
    }

    public CreditCardTransactionsRequest createCreditCardTransactionsRequestBody(Account account) {
        LocalDateTime fromTransactionDate =
                getDateForFetchHistoryTransactions(
                        account,
                        true,
                        transactionsFetchingDateFromManager.getComputedDateFrom().orElse(null));
        final TransactionDateEntity transactionDateEntity =
                new TransactionDateEntity(
                        YYYY_MM_DD_FORMAT.format(fromTransactionDate) + START_OF_DAY,
                        YYYY_MM_DD_FORMAT.format(LocalDateTime.now()) + END_OF_DAY);
        return CreditCardTransactionsRequest.builder()
                .withSortedBy(SORTED_BY)
                .withSortedType(SORTED_TYPE)
                .withCards(ImmutableList.of(new CardEntity().setId(account.getApiIdentifier())))
                .withShowContractTransactions(SHOW_CONTRACT_TRANSACTIONS)
                .withCustomerId(getUserId())
                .withSearchFilters(new SearchFiltersEntity(transactionDateEntity))
                .build();
    }

    public Optional<CreditCardTransactionsResponse> fetchCreditCardTransactions(
            Account account, String pageKey) {
        log.info("CreditCard Transaction Next Page: {}", pageKey);
        CreditCardTransactionsRequest body = createCreditCardTransactionsRequestBody(account);
        String transactionsUrl =
                isFirstPageOfAccountTransactions(pageKey)
                        ? Url.CREDIT_CARD_TRANSACTIONS
                        : Url.ASO + pageKey;
        try {
            return Optional.of(
                    createPostRequestInSession(transactionsUrl)
                            .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE))
                            .post(CreditCardTransactionsResponse.class, body));
        } catch (HttpResponseException e) {
            if (isEmptyCreditCardTransactions(e)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private boolean isEmptyCreditCardTransactions(HttpResponseException httpResponseException) {
        return httpResponseException != null
                && httpResponseException.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT;
    }

    public FinancialInvestmentResponse fetchFinancialInvestment(
            FinancialInvestmentRequest request) {
        return createPostRequestInSession(BbvaConstants.Url.FINANCIAL_INVESTMENTS)
                .post(FinancialInvestmentResponse.class, request);
    }

    public HistoricalDateResponse fetchInvestmentHistoricalDate(HistoricalDateRequest request) {
        return createPostRequestInSession(BbvaConstants.Url.HISTORICAL_DATE)
                .post(HistoricalDateResponse.class, request);
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId) {
        return createGetRequestInSession(
                        URL.of(BbvaConstants.Url.LOAN_DETAILS)
                                .parameter(BbvaConstants.Url.PARAM_ID, loanId)
                                .get())
                .get(LoanDetailsResponse.class);
    }

    public TransactionsRequest createAccountTransactionsDefaultRequestBody(Account account) {
        return TransactionsRequest.builder()
                .withCustomer(new UserEntity(getUserId()))
                .withSearchText(PostParameter.SEARCH_TEXT)
                .withOrderField(PostParameter.ORDER_FIELD)
                .withOrderType(PostParameter.ORDER_TYPE)
                .withAccountContracts(ImmutableList.of(getAccountContract(account)))
                .build();
    }

    public TransactionsRequest createAccountTransactionsRequestBody(Account account) {
        Optional<LocalDate> possibleDateFrom =
                transactionsFetchingDateFromManager.getComputedDateFrom();
        LocalDateTime fromTransactionDate =
                getDateForFetchHistoryTransactions(account, false, possibleDateFrom.orElse(null));
        final DateFilterEntity dateFilterEntity =
                new DateFilterEntity(
                        YYYY_MM_DD_FORMAT.format(fromTransactionDate) + START_OF_DAY,
                        YYYY_MM_DD_FORMAT.format(LocalDateTime.now()) + END_OF_DAY);
        return TransactionsRequest.builder()
                .withCustomer(new UserEntity(getUserId()))
                .withSearchText(PostParameter.SEARCH_TEXT)
                .withOrderField(PostParameter.ORDER_FIELD)
                .withOrderType(PostParameter.ORDER_TYPE)
                .withAccountContracts(ImmutableList.of(getAccountContract(account)))
                .withFilter(
                        new FilterEntity(
                                new AmountFilterEntity(),
                                dateFilterEntity,
                                Fetchers.OPERATION_TYPES))
                .build();
    }

    public IdentityDataResponse fetchIdentityData() {
        return createGetRequestInSession(
                        URL.of(Url.IDENTITY_DATA)
                                .parameter(BbvaConstants.Url.PARAM_ID, getUserId())
                                .get())
                .get(IdentityDataResponse.class);
    }

    public ParticipantsDataEntity fetchParticipants(String accountId) {
        return createGetRequestInSession(
                        URL.of(Url.PARTICIPANTS).parameter(Url.PARAM_ID, accountId).get())
                .get(ParticipantsDataEntity.class);
    }

    public AccountInfoEntity fetchMoreAccountInformation(String accountId) {
        return createGetRequestInSession(
                        URL.of(BbvaConstants.Url.IN_FORCE_CONDITIONS)
                                .parameter(Url.PARAM_ID, accountId)
                                .get())
                .get(AccountInfoEntity.class);
    }

    @SneakyThrows
    private AccountTransactionsResponse fetchAccountTransactionsWithOtp(
            HttpResponse exceptionResponse, TransactionsRequest request) {
        Retryer<AccountTransactionsResponse> fetchAccountTransactionsWithOtpRetryer =
                getFetchAccountTransactionsWithOtpRetryer();
        RequestBuilder otpBuilder =
                createAccountTransactionsHistoryRequestWithOtp(exceptionResponse);
        return fetchAccountTransactionsWithOtpRetryer.call(
                () -> retrieveAccountTransactionsWithOtp(request, otpBuilder));
    }

    private AccountTransactionsResponse retrieveAccountTransactionsWithOtp(
            TransactionsRequest request, RequestBuilder otpBuilder) {
        HttpResponse httpResponse = otpBuilder.post(HttpResponse.class, request);
        setTsec(httpResponse.getHeaders().getFirst(HeaderKeys.TSEC_KEY));
        return httpResponse.getBody(AccountTransactionsResponse.class);
    }

    private Retryer<AccountTransactionsResponse> getFetchAccountTransactionsWithOtpRetryer() {
        return RetryerBuilder.<AccountTransactionsResponse>newBuilder()
                .retryIfException(HttpClientException.class::isInstance)
                .withWaitStrategy(
                        WaitStrategies.fixedWait(
                                TIMEOUT_RETRY_SLEEP_MILLISECONDS, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }

    private RequestBuilder buildAccountTransactionRequest(String pageKey, boolean firstRequest) {
        if (isFirstPageOfAccountTransactions(pageKey)) {
            return createRequestOtpInSession(firstRequest)
                    .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                    .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
        }
        return createPaginationRequest(pageKey);
    }

    public void fetchUpdateTransactions(List<TransactionalAccount> accounts) {
        List<UpdateTransactionsContractEntity> contracts = new ArrayList<>();
        accounts.forEach(
                account ->
                        contracts.add(
                                new UpdateTransactionsContractEntity(account.getApiIdentifier())));
        UpdateTransactionsRequest updateTransactionsRequest =
                UpdateTransactionsRequest.builder().withContracts(contracts).build();
        createPostRequestInSession(Url.UPDATE_ACCOUNT_TRANSACTION)
                .post(UpdateTransactionsResponse.class, updateTransactionsRequest);
    }

    private RequestBuilder createPaginationRequest(String pageKey) {
        // check if key is url
        if (pageKey.contains("accountTransactions")) {
            return createPostRequestInSession(Url.ASO + pageKey);
        }
        return createPostRequestInSession(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                .queryParam(QueryKeys.PAGINATION_OFFSET, pageKey)
                .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
    }

    private RequestBuilder createAccountTransactionsHistoryRequestWithOtp(HttpResponse response) {
        String otp = supplementalInformationHelper.waitForOtpInput();
        if (otp != null && !otp.startsWith(OTP_SMS_SUFFIX)) {
            otp = OTP_SMS_SUFFIX + otp;
        }
        return createRequestOtpInSession(response, otp)
                .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
    }

    private RequestBuilder createRequestOtpInSession(boolean sendAuthenticationTypeHeader) {
        if (sendAuthenticationTypeHeader) {
            return createGetRequestInSession(Url.ACCOUNT_TRANSACTION)
                    .header(HeaderKeys.AUTHENTICATION_TYPE, PostParameter.AUTH_OTP_STATE);
        } else {
            return createGetRequestInSession(Url.ACCOUNT_TRANSACTION);
        }
    }

    private RequestBuilder createRequestOtpInSession(HttpResponse httpResponse, String otp) {
        String authenticationState =
                httpResponse.getHeaders().getFirst(HeaderKeys.AUTHENTICATION_STATE);
        String authenticationData =
                httpResponse.getHeaders().getFirst(HeaderKeys.AUTHENTICATION_DATA) + "=" + otp;
        return createRequestOtpInSession(true)
                .header(HeaderKeys.AUTHENTICATION_STATE, authenticationState)
                .header(HeaderKeys.AUTHENTICATION_DATA, authenticationData);
    }

    private RequestBuilder createGetRequestInSession(String url) {
        return createGetRequest(url).header(HeaderKeys.TSEC_KEY, getTsec());
    }

    private RequestBuilder createPostRequestInSession(String url) {
        return createPostRequest(url).header(HeaderKeys.TSEC_KEY, getTsec());
    }

    private RequestBuilder createGetRequest(String url) {
        return client.request(url).header(Headers.CHROME_UA).type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createPostRequest(String url) {
        return client.request(url)
                .header(Headers.CHROME_UA)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_JSON);
    }

    private LocalDateTime getDateForFetchHistoryTransactions(
            Account account, boolean isCreditCard, LocalDate possibleDateFrom) {
        LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(Defaults.TIMEZONE_CET));

        final int maxRegularConsentDaysBack = 88;
        if (possibleDateFrom != null) {
            if (possibleDateFrom.isBefore(LocalDate.now().minusDays(maxRegularConsentDaysBack))) {
                return LocalDateTime.of(possibleDateFrom, LocalTime.MIDNIGHT);
            }
            return currentDate.minusDays(maxRegularConsentDaysBack);
        }

        String accountId = account.getApiIdentifier();
        String openingAccountDateStr;
        LocalDateTime openingAccountDate;
        if (isCreditCard) {
            openingAccountDateStr = account.getFromTemporaryStorage(CARD_STAMP_DATE_KEY);
            long dateLong = Long.parseLong(openingAccountDateStr);
            openingAccountDate =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(dateLong), ZoneId.of(Defaults.TIMEZONE_CET));

        } else {
            openingAccountDateStr = sessionStorage.get(accountId + OPENING_DATE_KEY);
            if (Strings.isNullOrEmpty(openingAccountDateStr)) {
                openingAccountDateStr = fetchMoreAccountInformation(accountId).getOpeningDate();
                sessionStorage.put(accountId + OPENING_DATE_KEY, openingAccountDateStr);
            }
            openingAccountDate =
                    LocalDateTime.of(LocalDate.parse(openingAccountDateStr), LocalTime.MAX);
        }

        LocalDateTime maximumDate =
                currentDate
                        .minusMonths(BbvaConstants.Fetchers.MAX_NUM_MONTHS_FOR_FETCH)
                        .withDayOfMonth(1);

        if (openingAccountDate.isAfter(maximumDate)) {
            return currentDate.minusDays(ChronoUnit.DAYS.between(openingAccountDate, currentDate));
        }
        return currentDate.minusDays(ChronoUnit.DAYS.between(maximumDate, currentDate));
    }

    private AccountContractsEntity getAccountContract(Account account) {
        final String accountId = account.getApiIdentifier();
        return new AccountContractsEntity(new ContractEntity().setId(accountId));
    }

    private boolean isSecondFactorAuthenticationNeeded(BbvaErrorResponse errorResponse) {
        return ErrorCode.OTP_VERIFICATION_CODE.equals(errorResponse.getErrorCode())
                && ErrorCode.OTP_SYSTEM_ERROR_CODE.equals(errorResponse.getSystemErrorCode());
    }

    public boolean isFirstPageOfAccountTransactions(String pageKey) {
        return Strings.isNullOrEmpty(pageKey);
    }

    private String getTsec() {
        return sessionStorage.get(BbvaConstants.HeaderKeys.TSEC_KEY);
    }

    private void setTsec(String tsec) {
        sessionStorage.put(BbvaConstants.HeaderKeys.TSEC_KEY, tsec);
    }

    public String getUserId() {
        return sessionStorage.get(BbvaConstants.StorageKeys.USER_ID);
    }

    private void setUserId(String userId) {
        sessionStorage.put(BbvaConstants.StorageKeys.USER_ID, userId);
    }

    public void requestMoreThan90DaysTransactionsForFirstAccount(
            Collection<? extends Account> accounts) {
        accounts.stream()
                .findFirst()
                .ifPresent(firstAccount -> fetchAccountTransactionsToForceOtp(firstAccount, ""));
    }
}
