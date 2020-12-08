package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.UserData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.StatusResponse;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class LaCaixaApiClient {

    private final TinkHttpClient client;
    private final String installationId;

    private UserDataResponse userDataCache;

    public LaCaixaApiClient(TinkHttpClient client, String installationId) {
        this.client = client;
        this.installationId = installationId;
    }

    public SessionResponse initializeSession() {

        SessionRequest request =
                new SessionRequest(
                        LaCaixaConstants.DefaultRequestParams.LANGUAGE_ES,
                        LaCaixaConstants.DefaultRequestParams.ORIGIN,
                        LaCaixaConstants.DefaultRequestParams.CHANNEL,
                        installationId);

        return createRequest(LaCaixaConstants.Urls.INIT_LOGIN).post(SessionResponse.class, request);
    }

    public StatusResponse login(LoginRequest loginRequest) throws LoginException {

        try {

            return createRequest(LaCaixaConstants.Urls.SUBMIT_LOGIN)
                    .post(StatusResponse.class, loginRequest);

        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatus();

            if (statusCode == LaCaixaConstants.StatusCodes.INCORRECT_USERNAME_PASSWORD) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }

            throw e;
        }
    }

    public void logout() {
        createRequest(LaCaixaConstants.Urls.LOGOUT).post();
    }

    public ListAccountsResponse fetchAccountList() {

        return createRequest(LaCaixaConstants.Urls.MAIN_ACCOUNT).get(ListAccountsResponse.class);
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

    public CardTransactionsResponse fetchCardTransactions(String cardId, boolean start) {
        return createRequest(LaCaixaConstants.Urls.CARD_TRANSACTIONS)
                .body(new CardTransactionsRequest(cardId, start))
                .post(CardTransactionsResponse.class);
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
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }
}
