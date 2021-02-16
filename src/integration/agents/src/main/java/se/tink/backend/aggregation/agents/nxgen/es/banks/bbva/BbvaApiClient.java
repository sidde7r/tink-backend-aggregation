package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountContractsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.DateFilterEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.FilterEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class BbvaApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String userAgent;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PersistentStorage persistentStorage;
    private String openingAccountDateStr;

    public BbvaApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.persistentStorage = persistentStorage;
        this.userAgent =
                String.format(Headers.BBVA_USER_AGENT.getValue(), BbvaUtils.generateRandomHex());
    }

    public HttpResponse isAlive() {
        return createRequestInSession(BbvaConstants.Url.REFRESH_TICKET)
                .accept(MediaType.WILDCARD_TYPE)
                .queryParam(QueryKeys.ISALIVE_CUSTOMER_ID, getUserId())
                .post(HttpResponse.class);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        HttpResponse httpResponse =
                createRequest(BbvaConstants.Url.TICKET)
                        .header(Headers.CONSUMER_ID)
                        .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent())
                        .post(HttpResponse.class, loginRequest);
        LoginResponse loginResponse = httpResponse.getBody(LoginResponse.class);

        setTsec(httpResponse.getHeaders().getFirst(HeaderKeys.TSEC_KEY));
        setUserId(loginResponse.getUser().getId());
        return loginResponse;
    }

    public void logout() {
        createRequest(BbvaConstants.Url.TICKET)
                .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent())
                .header(HeaderKeys.TSEC_KEY, getTsec())
                .delete();
    }

    public FinancialDashboardResponse fetchFinancialDashboard() {
        return createRequestInSession(BbvaConstants.Url.FINANCIAL_DASHBOARD)
                .queryParam(QueryKeys.DASHBOARD_CUSTOMER_ID, getUserId())
                .queryParam(QueryKeys.DASHBOARD_FILTER, QueryValues.DASHBOARD_FILTER)
                .get(FinancialDashboardResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransactions(Account account, String pageKey) {
        boolean firstLogin = isFirstLogin();
        final TransactionsRequest request = createAccountTransactionsRequest(account, firstLogin);
        final RequestBuilder builder = buildAccountTransactionRequest(pageKey, firstLogin);
        try {
            return builder.post(AccountTransactionsResponse.class, request);
        } catch (HttpResponseException e) {
            HttpResponse exceptionResponse = e.getResponse();
            BbvaErrorResponse errorResponse = exceptionResponse.getBody(BbvaErrorResponse.class);
            if (isSecondFactorAuthenticationNeeded(errorResponse)) {
                return fetchAccountTransactionsWithOtp(exceptionResponse, request);
            }
            if (errorResponse.isConflictStatus() || errorResponse.isInternalServerError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            log.info(
                    "Unknown error: httpStatus {}, code {}, message {}",
                    errorResponse.getHttpStatus(),
                    errorResponse.getErrorCode(),
                    errorResponse.getErrorMessage());
            throw BankServiceError.DEFAULT_MESSAGE.exception();
        }
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            Account account, String keyIndex) {
        return createRequestInSession(BbvaConstants.Url.CREDIT_CARD_TRANSACTIONS)
                .queryParam(QueryKeys.CONTRACT_ID, account.getApiIdentifier())
                .queryParam(
                        QueryKeys.CARD_TRANSACTION_TYPE,
                        BbvaConstants.AccountType.CREDIT_CARD_SHORT_TYPE)
                .queryParam(QueryKeys.PAGINATION_OFFSET, keyIndex)
                .get(CreditCardTransactionsResponse.class);
    }

    public FinancialInvestmentResponse fetchFinancialInvestment(
            FinancialInvestmentRequest request) {
        return createRequestInSession(BbvaConstants.Url.FINANCIAL_INVESTMENTS)
                .post(FinancialInvestmentResponse.class, request);
    }

    public HistoricalDateResponse fetchInvestmentHistoricalDate(HistoricalDateRequest request) {
        try {
            return createRequestInSession(BbvaConstants.Url.HISTORICAL_DATE)
                    .post(HistoricalDateResponse.class, request);
        } catch (HttpResponseException ex) {
            BbvaErrorResponse response = ex.getResponse().getBody(BbvaErrorResponse.class);
            if (response.isContractNotOperableError()) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(response.getErrorMessage());
            }
            log.info(
                    "Unknown error: httpStatus {}, code {}, message {}",
                    response.getHttpStatus(),
                    response.getErrorCode(),
                    response.getErrorMessage());
            throw BankServiceError.DEFAULT_MESSAGE.exception();
        }
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId) {
        return createRequestInSession(
                        URL.of(BbvaConstants.Url.LOAN_DETAILS)
                                .parameter(BbvaConstants.Url.PARAM_ID, loanId)
                                .get())
                .get(LoanDetailsResponse.class);
    }

    public TransactionsRequest createAccountTransactionsRequest(
            Account account, boolean firstLogin) {
        if (firstLogin) {
            LocalDateTime fromTransactionDate =
                    getDateForFetchHistoryTransactions(account.getApiIdentifier());
            LocalDateTime toTransactionDate = LocalDateTime.now(Fetchers.CLOCK);
            return createAccountTransactionsAllHistoryRequest(
                    account, fromTransactionDate.toString(), toTransactionDate.toString());
        }
        return TransactionsRequest.builder()
                .withCustomer(new UserEntity(getUserId()))
                .withSearchType(BbvaConstants.PostParameter.SEARCH_TYPE)
                .withAccountContracts(ImmutableList.of(getAccountContract(account)))
                .build();
    }

    public IdentityDataResponse fetchIdentityData() {
        return createRequestInSession(
                        URL.of(Url.IDENTITY_DATA)
                                .parameter(BbvaConstants.Url.PARAM_ID, getUserId())
                                .get())
                .get(IdentityDataResponse.class);
    }

    public ParticipantsDataEntity fetchParticipants(String accountId) {
        return createRequestInSession(
                        URL.of(Url.PARTICIPANTS).parameter(Url.PARAM_ID, accountId).get())
                .get(ParticipantsDataEntity.class);
    }

    public AccountInfoEntity fetchMoreAccountInformation(String accountId) {
        return createRequestInSession(
                        URL.of(BbvaConstants.Url.IN_FORCE_CONDITIONS)
                                .parameter(Url.PARAM_ID, accountId)
                                .get())
                .get(AccountInfoEntity.class);
    }

    private AccountTransactionsResponse fetchAccountTransactionsWithOtp(
            HttpResponse exceptionResponse, TransactionsRequest request) {
        RequestBuilder otpBuilder =
                createAccountTransactionsHistoryRequestWithOtp(exceptionResponse);
        HttpResponse httpResponse = otpBuilder.post(HttpResponse.class, request);

        setTsec(httpResponse.getHeaders().getFirst(HeaderKeys.TSEC_KEY));

        return httpResponse.getBody(AccountTransactionsResponse.class);
    }

    private RequestBuilder buildAccountTransactionRequest(String pageKey, boolean firstLogin) {
        if (firstLogin && isFirstPageOfAccountTransactions(pageKey)) {
            return createRequestOtpInSession(Url.ACCOUNT_TRANSACTION)
                    .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                    .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
        }
        if (isFirstPageOfAccountTransactions(pageKey)) {
            return createRequestInSession(BbvaConstants.Url.ACCOUNT_TRANSACTION)
                    .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                    .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
        }
        return createRequestInSession(Url.ASO + pageKey);
    }

    private RequestBuilder createAccountTransactionsHistoryRequestWithOtp(HttpResponse response) {
        String otp = supplementalInformationHelper.waitForOtpInput();
        return createRequestOtpInSession(Url.ACCOUNT_TRANSACTION, response, otp)
                .queryParam(QueryKeys.PAGINATION_OFFSET, QueryValues.FIRST_PAGE_KEY)
                .queryParam(QueryKeys.PAGE_SIZE, String.valueOf(Fetchers.PAGE_SIZE));
    }

    private RequestBuilder createRequestOtpInSession(String url) {
        return createRequestInSession(url)
                .header(HeaderKeys.AUTHENTICATION_TYPE, PostParameter.AUTH_OTP_STATE);
    }

    private RequestBuilder createRequestOtpInSession(
            String url, HttpResponse httpResponse, String otp) {
        String authenticationState =
                httpResponse.getHeaders().getFirst(HeaderKeys.AUTHENTICATION_STATE);
        String authenticationData =
                httpResponse.getHeaders().getFirst(HeaderKeys.AUTHENTICATION_DATA) + "=" + otp;
        return createRequestOtpInSession(url)
                .header(HeaderKeys.AUTHENTICATION_STATE, authenticationState)
                .header(HeaderKeys.AUTHENTICATION_DATA, authenticationData);
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequest(url)
                .header(Headers.REFERER)
                .header(HeaderKeys.TSEC_KEY, getTsec())
                .header(Headers.BBVA_USER_AGENT.getKey(), getUserAgent());
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private LocalDateTime getDateForFetchHistoryTransactions(String accountId) {
        LocalDateTime currentDate = LocalDateTime.now(Fetchers.CLOCK);
        if (Strings.isNullOrEmpty(this.openingAccountDateStr)) {
            this.openingAccountDateStr = fetchMoreAccountInformation(accountId).getOpeningDate();
        }
        LocalDateTime openingAccountDate =
                LocalDateTime.of(LocalDate.parse(this.openingAccountDateStr), LocalTime.MAX);
        LocalDateTime maximumDate =
                currentDate
                        .minusMonths(BbvaConstants.Fetchers.MAX_NUM_MONTHS_FOR_FETCH)
                        .withDayOfMonth(1);

        if (openingAccountDate.isAfter(maximumDate)) {
            return currentDate.minusDays(ChronoUnit.DAYS.between(openingAccountDate, currentDate));
        }
        return currentDate.minusDays(ChronoUnit.DAYS.between(maximumDate, currentDate));
    }

    private TransactionsRequest createAccountTransactionsAllHistoryRequest(
            Account account, String fromDate, String toDate) {
        final DateFilterEntity dateFilterEntity = new DateFilterEntity(fromDate, toDate);

        return TransactionsRequest.builder()
                .withCustomer(new UserEntity(getUserId()))
                .withSearchType(BbvaConstants.PostParameter.SEARCH_TYPE)
                .withAccountContracts(ImmutableList.of(getAccountContract(account)))
                .withError(true)
                .withFilter(new FilterEntity(dateFilterEntity, Fetchers.OPERATION_TYPES))
                .build();
    }

    private AccountContractsEntity getAccountContract(Account account) {
        final String accountId = account.getApiIdentifier();
        return new AccountContractsEntity(new ContractEntity().setId(accountId));
    }

    private boolean isSecondFactorAuthenticationNeeded(BbvaErrorResponse errorResponse) {
        return ErrorCode.OTP_VERIFICATION_CODE.equals(errorResponse.getErrorCode())
                && ErrorCode.OTP_SYSTEM_ERROR_CODE.equals(errorResponse.getSystemErrorCode());
    }

    private boolean isFirstPageOfAccountTransactions(String pageKey) {
        return Strings.isNullOrEmpty(pageKey);
    }

    private boolean isFirstLogin() {
        return Boolean.parseBoolean(persistentStorage.get(Defaults.FIRST_LOGIN));
    }

    private String getUserAgent() {
        return userAgent;
    }

    private String getTsec() {
        return sessionStorage.get(BbvaConstants.StorageKeys.TSEC);
    }

    private void setTsec(String tsec) {
        sessionStorage.put(BbvaConstants.StorageKeys.TSEC, tsec);
    }

    public String getUserId() {
        return sessionStorage.get(BbvaConstants.StorageKeys.USER_ID);
    }

    private void setUserId(String userId) {
        sessionStorage.put(BbvaConstants.StorageKeys.USER_ID, userId);
    }
}
