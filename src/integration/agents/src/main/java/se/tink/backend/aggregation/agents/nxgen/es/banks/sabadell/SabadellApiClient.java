package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Urls.FETCH_ACCOUNT_HOLDERS;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.InitiateSessionRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SecurityInputEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.CreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.DepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.MarketsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.MarketsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.PensionPlansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.SavingsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.StocksResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountHoldersResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SabadellApiClient {

    private final TinkHttpClient client;
    private final Filter bankServiceErrorFilter;

    public SabadellApiClient(TinkHttpClient client) {
        this.client = client;
        client.addMessageWriter(
                new NoEscapeOfBackslashMessageBodyWriter(InitiateSessionRequestEntity.class));
        bankServiceErrorFilter = new BankServiceInternalErrorFilter();
        client.addFilter(bankServiceErrorFilter);
    }

    public SessionResponse initiateSession(
            String username,
            String password,
            String csid,
            @Nullable SecurityInputEntity securityInput)
            throws LoginException {
        final InitiateSessionRequestEntity requestEntity =
                InitiateSessionRequestEntity.build(username, password, csid);
        if (Objects.nonNull(securityInput)) {
            requestEntity.setSecurityInput(securityInput);
        }

        try {
            return createRequest(Urls.INITIATE_SESSION)
                    .removeFilter(bankServiceErrorFilter)
                    .post(SessionResponse.class, requestEntity);
        } catch (HttpResponseException e) {
            HttpResponse httpResponse = e.getResponse();
            ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
            String errorCode = errorResponse.getErrorCode();

            switch (errorCode) {
                case ErrorCodes.INCORRECT_CREDENTIALS:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case ErrorCodes.EXPIRED_CARD:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                default:
                    String message =
                            String.format(
                                    "%s: Login failed with http status: %s, error code: %s, error message: %s",
                                    Tags.LOGIN_ERROR,
                                    httpResponse.getStatus(),
                                    errorCode,
                                    errorResponse.getErrorMessage());
                    throw LoginError.DEFAULT_MESSAGE.exception(message);
            }
        }
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(Urls.FETCH_ACCOUNTS)
                .queryParam(QueryParamPairs.NO_ERROR.getKey(), QueryParamPairs.NO_ERROR.getValue())
                .queryParam(
                        QueryParamPairs.CTA_VISTA.getKey(), QueryParamPairs.CTA_VISTA.getValue())
                .get(AccountsResponse.class);
    }

    public FetchCreditCardsResponse fetchCreditCards() {
        return createRequest(Urls.FETCH_CREDIT_CARDS)
                .queryParam(
                        QueryParamPairs.CTA_CARD_ALL.getKey(),
                        QueryParamPairs.CTA_CARD_ALL.getValue())
                .get(FetchCreditCardsResponse.class);
    }

    public AccountTransactionsResponse fetchTransactions(AccountTransactionsRequest requestEntity) {
        return createRequest(Urls.FETCH_ACCOUNT_TRANSACTIONS)
                .post(AccountTransactionsResponse.class, requestEntity);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            CreditCardEntity creditCardEntity, int totalItems, int page) {

        CreditCardTransactionsRequest requestEntity =
                CreditCardTransactionsRequest.build(creditCardEntity, totalItems, page);

        return createRequest(Urls.FETCH_CREDIT_CARD_TRANSACTIONS)
                .post(CreditCardTransactionsResponse.class, requestEntity);
    }

    public LoansResponse fetchLoans() {
        return createRequest(Urls.FETCH_LOANS)
                .queryParam(QueryParamPairs.PAGE.getKey(), QueryParamPairs.PAGE.getValue())
                .queryParam(FetcherRequest.ITEMS_PER_PAGE, LoansRequest.ITEMS_PER_PAGE)
                .queryParam(
                        QueryParamPairs.ORDER_DESC.getKey(), QueryParamPairs.ORDER_DESC.getValue())
                .removeFilter(bankServiceErrorFilter)
                .get(LoansResponse.class);
    }

    public DepositsResponse fetchDeposits() {
        return createRequest(Urls.FETCH_DEPOSITS).get(DepositsResponse.class);
    }

    public ServicingFundsResponse fetchServicingFunds() {
        return createRequest(Urls.FETCH_SERVICING_FUNDS).get(ServicingFundsResponse.class);
    }

    public PensionPlansResponse fetchPensionPlans() {
        return createRequest(Urls.FETCH_PENSION_PLANS)
                .queryParam(QueryParamPairs.NO_ERROR.getKey(), QueryParamPairs.NO_ERROR.getValue())
                .queryParam(QueryParamPairs.PAGE.getKey(), QueryParamPairs.PAGE.getValue())
                .queryParam(FetcherRequest.ITEMS_PER_PAGE, PensionPlansRequest.ITEMS_PER_PAGE)
                .queryParam(QueryParamPairs.ORDER_0.getKey(), QueryParamPairs.ORDER_0.getValue())
                .get(PensionPlansResponse.class);
    }

    public SavingsResponse fetchSavings() {
        return createRequest(Urls.FETCH_SAVINGS).get(SavingsResponse.class);
    }

    public void logout() {
        createRequest(Urls.INITIATE_SESSION).delete();
    }

    public ProductsResponse fetchProducts() {
        return createRequest(Urls.FETCH_PRODUCTS)
                .queryParam(QueryParamPairs.NO_ERROR.getKey(), QueryParamPairs.NO_ERROR.getValue())
                .get(ProductsResponse.class);
    }

    public MarketsResponse fetchMarkets(MarketsRequest request) {
        return createRequest(Urls.FETCH_MARKETS).post(MarketsResponse.class, request);
    }

    public StocksResponse fetchStocks(String market, Map<String, String> queryParams) {
        return createRequest(Urls.FETCH_STOCKS.parameter(UrlParams.MARKET, market))
                .queryParams(queryParams)
                .get(StocksResponse.class);
    }

    public String fetchServicingFundsAccountDetails(ServicingFundsAccountDetailsRequest request) {
        return createRequest(Urls.FETCH_SERVICING_FUNDS_ACCOUNT_DETAILS)
                .post(String.class, request);
    }

    public String fetchSavingsPlanDetails(Map<String, String> queryParams) {
        return createRequest(Urls.FETCH_SAVINGS_PLAN_DETAILS)
                .queryParams(queryParams)
                .get(String.class);
    }

    public LoanDetailsResponse fetchLoanDetails(LoanDetailsRequest loanDetailsRequest) {
        return createRequest(Urls.FETCH_LOAN_DETAILS)
                .post(LoanDetailsResponse.class, loanDetailsRequest);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .acceptLanguage(Headers.ACCEPT_LANGUAGE)
                .accept(Headers.SABADELL_ACCEPT);
    }

    public AccountHoldersResponse fetchAccountHolders(AccountEntity accountEntity) {
        AccountRequest accountRequest = new AccountRequest(accountEntity);
        return client.request(FETCH_ACCOUNT_HOLDERS)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .acceptLanguage(Headers.ACCEPT_LANGUAGE)
                .accept(Headers.SABADELL_ACCEPT_ACCOUNT_HOLDERS)
                .post(AccountHoldersResponse.class, accountRequest);
    }
}
