package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.OTPSMS_AUTH;

import com.google.common.base.Strings;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.PermStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.StatusResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.VerifySignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardLiquidationsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardLiquidationsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.GenericCardsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.GenericCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.LiquidationDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc.LiquidationDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.EngagementResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundsListRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundsListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.PositionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.PositionValuesResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.caixa.utils.CaixaRegistrationDataGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class LaCaixaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String username;
    private final RandomValueGenerator randomValueGenerator;

    private UserDataResponse userDataCache;

    public SessionResponse initializeSession() {

        SessionRequest request =
                new SessionRequest(
                        LaCaixaConstants.DefaultRequestParams.LANGUAGE_ES,
                        LaCaixaConstants.DefaultRequestParams.ORIGIN,
                        LaCaixaConstants.DefaultRequestParams.CHANNEL,
                        retrieveAppInstallationId());

        HttpResponse response =
                createRequest(LaCaixaConstants.Urls.INIT_LOGIN).post(HttpResponse.class, request);

        return response.getBody(SessionResponse.class);
    }

    public StatusResponse login(LoginRequest loginRequest) throws LoginException {
        try {
            return createRequest(LaCaixaConstants.Urls.SUBMIT_LOGIN)
                    .post(StatusResponse.class, loginRequest);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            if (response.getStatus() >= HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            if (response.getStatus() >= HttpStatus.SC_BAD_REQUEST
                    && response.getStatus() < HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);
                if (errorResponse.isAccountBlocked()) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                } else if (errorResponse.isIdentificationIncorrect()) {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                } else if (errorResponse.isTemporaryProblem()
                        || errorResponse.isCurrentlyUnavailable()) {
                    throw BankServiceError.NO_BANK_SERVICE.exception();
                }
                log.info(
                        "Unknown error code {} with message {}",
                        errorResponse.getCode(),
                        errorResponse.getMessage());
            }
            throw LoginError.DEFAULT_MESSAGE.exception();
        }
    }

    public void logout() {
        createRequest(LaCaixaConstants.Urls.LOGOUT).post();
    }

    public ListAccountsResponse fetchAccountList() {

        return createRequest(LaCaixaConstants.Urls.MAIN_ACCOUNT).get(ListAccountsResponse.class);
    }

    public ListHoldersResponse fetchHolderList(String accountReference) {
        return createRequest(LaCaixaConstants.Urls.HOLDERS_LIST)
                .queryParam(LaCaixaConstants.QueryParams.FROM_BEGIN, "true")
                .queryParam(LaCaixaConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(ListHoldersResponse.class);
    }

    public UserDataResponse fetchIdentityData() {
        if (userDataCache != null) {
            return userDataCache;
        }

        UserDataRequest request =
                new UserDataRequest(
                        LaCaixaConstants.UserData.FULL_HOLDER_NAME,
                        LaCaixaConstants.UserData.DNI,
                        UserData.DATE_OF_BIRTH,
                        UserData.FIRST_NAME,
                        UserData.FIRST_SUR_NAME,
                        UserData.SECOND_SUR_NAME);

        UserDataResponse userDataResponse =
                createRequest(Urls.USER_DATA).post(UserDataResponse.class, request);

        userDataCache = userDataResponse;
        return userDataResponse;
    }

    public AccountTransactionResponse fetchNextAccountTransactions(
            String accountReference, boolean fromBegin) {

        return createRequest(LaCaixaConstants.Urls.ACCOUNT_TRANSACTIONS)
                .queryParam(LaCaixaConstants.QueryParams.FROM_BEGIN, Boolean.toString(fromBegin))
                .queryParam(LaCaixaConstants.QueryParams.ACCOUNT_NUMBER, accountReference)
                .get(AccountTransactionResponse.class);
    }

    public EngagementResponse fetchEngagements(String globalPositionType) {
        return createRequest(LaCaixaConstants.Urls.ENGAGEMENTS)
                .queryParam(
                        LaCaixaConstants.QueryParams.ZERO_BALANCE_CONTRACTS,
                        LaCaixaConstants.DefaultRequestParams.ZERO_BALANCE_CONTRACTS)
                .queryParam(LaCaixaConstants.QueryParams.GLOBAL_POSITION_TYPE, globalPositionType)
                .get(EngagementResponse.class);
    }

    public FundsListResponse fetchFundList(boolean moreData) {
        FundsListRequest request = new FundsListRequest(moreData);
        return createRequest(LaCaixaConstants.Urls.FUND_LIST)
                .post(FundsListResponse.class, request);
    }

    public FundDetailsResponse fetchFundDetails(
            String fundReference, String fundCode, String currency) {
        FundDetailsRequest request = new FundDetailsRequest(fundReference, fundCode, currency);
        return createRequest(LaCaixaConstants.Urls.FUND_DETAILS)
                .post(FundDetailsResponse.class, request);
    }

    public PositionValuesResponse fetchDepositList() {
        return createRequest(LaCaixaConstants.Urls.DEPOSIT_LIST).get(PositionValuesResponse.class);
    }

    public PositionValuesResponse fetchDeposit(String depositId, boolean moreData) {
        return createRequest(LaCaixaConstants.Urls.DEPOSIT_LIST)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_ID, depositId)
                .queryParam(LaCaixaConstants.QueryParams.MORE_DATA, Boolean.toString(moreData))
                .get(PositionValuesResponse.class);
    }

    public PositionDetailsResponse fetchPositionDetails(String depositId, String depositContentId) {
        return createRequest(LaCaixaConstants.Urls.DEPOSIT_DETAILS)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_CONTENT_ID, depositContentId)
                .queryParam(LaCaixaConstants.QueryParams.DEPOSIT_ID, depositId)
                .get(PositionDetailsResponse.class);
    }

    public GenericCardsResponse fetchCards() {
        return createRequest(LaCaixaConstants.Urls.CARDS)
                .body(
                        new GenericCardsRequest(
                                true, LaCaixaConstants.DefaultRequestParams.NUM_CARDS))
                .post(GenericCardsResponse.class);
    }

    public CardTransactionsResponse fetchCardTransactions(
            CreditCardAccount account, boolean start) {
        return createRequest(LaCaixaConstants.Urls.CARD_TRANSACTIONS)
                .body(new CardTransactionsRequest(account.getApiIdentifier(), start))
                .post(CardTransactionsResponse.class)
                .setAccount(account);
    }

    public CardLiquidationsResponse fetchCardLiquidations(String contractRefVal, boolean start) {
        return createRequest(LaCaixaConstants.Urls.CARD_LIQUIDATIONS)
                .body(new CardLiquidationsRequest(contractRefVal, start))
                .post(CardLiquidationsResponse.class);
    }

    public LiquidationDetailResponse fetchCardLiquidationDetail(
            String contractRefNum, String liquidationDate) {
        return createRequest(Urls.CARD_LIQUIDATION_DETAILS)
                .body(new LiquidationDetailRequest(contractRefNum, liquidationDate))
                .post(LiquidationDetailResponse.class);
    }

    public LoanListResponse fetchLoansList(boolean fromBegin) {

        return createRequest(LaCaixaConstants.Urls.LOAN_LIST)
                .queryParam(LaCaixaConstants.QueryParams.FROM_BEGIN, Boolean.toString(fromBegin))
                .get(LoanListResponse.class);
    }

    public LoanDetailsResponse fetchMortgageDetails(String loanId) {
        return fetchLoanDetails(Urls.MORTGAGE_DETAILS, loanId);
    }

    public LoanDetailsResponse fetchConsumerLoanDetails(String loanId) {
        return fetchLoanDetails(Urls.CONSUMER_LOAN_DETAILS, loanId);
    }

    private LoanDetailsResponse fetchLoanDetails(URL url, String loanId) {
        LoanDetailsRequest loanDetailsRequest = new LoanDetailsRequest(loanId);

        return createRequest(url).post(LoanDetailsResponse.class, loanDetailsRequest);
    }

    public boolean isAlive() {

        try {

            createRequest(LaCaixaConstants.Urls.KEEP_ALIVE).get(HttpResponse.class);
        } catch (HttpResponseException e) {

            return false;
        }

        return true;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.USER_AGENT, retrieveUserAgent())
                .header(
                        HeaderKeys.X_REQUEST_ID,
                        randomValueGenerator.getUUID().toString().toUpperCase())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public ScaResponse initiateEnrolment() {
        return createRequest(Urls.INIT_ENROLMENT).get(ScaResponse.class);
    }

    public HttpResponse initiateAuthSignature() {
        try {
            return createRequest(Urls.INIT_AUTH_SIGNATURE).post(HttpResponse.class);
        } catch (HttpResponseException e) {
            return e.getResponse();
        }
    }

    public VerifySignatureResponse verifyAuthSignature() {
        return createRequest(Urls.VERIFY_AUTH_SIGNATURE).get(VerifySignatureResponse.class);
    }

    public ScaResponse authorizeSCA(String code) {
        final AuthenticationRequest body = new AuthenticationRequest(code);
        try {
            return createRequest(Urls.AUTHORIZE_SCA).post(ScaResponse.class, body);
        } catch (HttpResponseException e) {
            HttpResponse exceptionResponse = e.getResponse();
            LaCaixaErrorResponse errorResponse =
                    exceptionResponse.getBody(LaCaixaErrorResponse.class);
            if (errorResponse.getMessage().contains(OTPSMS_AUTH)) {
                return initiateEnrolment();
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        } catch (HttpClientException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    public StatusResponse finalizeEnrolment(@Nullable String code) {
        final AuthenticationRequest body = new AuthenticationRequest(Strings.nullToEmpty(code));
        return createRequest(Urls.FINALIZE_ENROLMENT).post(StatusResponse.class, body);
    }

    public LoginResultResponse checkLoginResult(LoginRequest loginRequest) {
        return createRequest(Urls.CHECK_LOGIN_RESULT).post(LoginResultResponse.class, loginRequest);
    }

    public String checkIfScaNeeded() {
        UserDataRequest request = new UserDataRequest(UserData.IS_ENROLLMENT_NEEDED);

        UserDataResponse userDataResponse =
                createRequest(Urls.USER_DATA).post(UserDataResponse.class, request);

        return userDataResponse.isScaNeeded();
    }

    private String retrieveUserAgent() {
        String userAgent = persistentStorage.get(PermStorage.USER_AGENT);
        if (userAgent == null) {
            userAgent =
                    CaixaRegistrationDataGenerator.generateUserAgent(
                            "APPCBK_", "5.43.0", retrieveAppInstallationId());
            if (StringUtils.isNotEmpty(username)) {
                persistentStorage.put(PermStorage.USER_AGENT, userAgent);
            }
        }
        return userAgent;
    }

    private String retrieveAppInstallationId() {
        String appInstallationId = persistentStorage.get(PermStorage.APP_INSTALLATION_ID);
        if (appInstallationId == null) {
            appInstallationId =
                    CaixaRegistrationDataGenerator.generateAppInstallationId(
                            "com.thenetfirm.mobile.wapicon.WapIcon_iPhone",
                            username,
                            "c",
                            Base64.getEncoder());
            if (StringUtils.isNotEmpty(username)) {
                persistentStorage.put(PermStorage.APP_INSTALLATION_ID, appInstallationId);
            }
        }
        return appInstallationId;
    }
}
