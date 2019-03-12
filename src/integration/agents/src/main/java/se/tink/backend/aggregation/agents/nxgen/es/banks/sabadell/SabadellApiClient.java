package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.InitiateSessionRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.CreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.DepositsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.PensionPlansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.SavingsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsAccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ServicingFundsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

import javax.ws.rs.core.MediaType;

public class SabadellApiClient {
    private final TinkHttpClient client;

    public SabadellApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(SabadellConstants.Headers.SABADELL_ACCEPT);
    }

    public void initiateSession(String username, String password) {
        InitiateSessionRequestEntity requestEntity =
                InitiateSessionRequestEntity.build(username, password);

        createRequest(SabadellConstants.Urls.INITIATE_SESSION)
                .post(HttpResponse.class, requestEntity);
    }

    public AccountsResponse fetchAccounts() {
        return createRequest(SabadellConstants.Urls.FETCH_ACCOUNTS)
                .queryParam(
                        SabadellConstants.QueryParamPairs.NO_ERROR.getKey(),
                        SabadellConstants.QueryParamPairs.NO_ERROR.getValue())
                .queryParam(
                        SabadellConstants.QueryParamPairs.CTA_VISTA.getKey(),
                        SabadellConstants.QueryParamPairs.CTA_VISTA.getValue())
                .get(AccountsResponse.class);
    }

    public FetchCreditCardsResponse fetchCreditCards() {
        return createRequest(SabadellConstants.Urls.FETCH_CREDIT_CARDS)
                .queryParam(
                        SabadellConstants.QueryParamPairs.CTA_CARD_ALL.getKey(),
                        SabadellConstants.QueryParamPairs.CTA_CARD_ALL.getValue())
                .get(FetchCreditCardsResponse.class);
    }

    public AccountTransactionsResponse fetchTransactions(AccountEntity accountEntity, boolean key) {
        AccountTransactionsRequest requestEntity =
                AccountTransactionsRequest.build(accountEntity, key);

        return createRequest(SabadellConstants.Urls.FETCH_ACCOUNT_TRANSACTIONS)
                .post(AccountTransactionsResponse.class, requestEntity);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(
            CreditCardEntity creditCardEntity, int totalItems, int page) {

        CreditCardTransactionsRequest requestEntity =
                CreditCardTransactionsRequest.build(creditCardEntity, totalItems, page);

        return createRequest(SabadellConstants.Urls.FETCH_CREDIT_CARD_TRANSACTIONS)
                .post(CreditCardTransactionsResponse.class, requestEntity);
    }

    public String fetchLoans() {
        return createRequest(SabadellConstants.Urls.FETCH_LOANS)
                .queryParam(
                        SabadellConstants.QueryParamPairs.PAGE.getKey(),
                        SabadellConstants.QueryParamPairs.PAGE.getValue())
                .queryParam(
                        SabadellConstants.FetcherRequest.ITEMS_PER_PAGE,
                        SabadellConstants.LoansRequest.ITEMS_PER_PAGE)
                .queryParam(
                        SabadellConstants.QueryParamPairs.ORDER_DESC.getKey(),
                        SabadellConstants.QueryParamPairs.ORDER_DESC.getValue())
                .get(String.class);
    }

    public DepositsResponse fetchDeposits() {
        return createRequest(SabadellConstants.Urls.FETCH_DEPOSITS).get(DepositsResponse.class);
    }

    public ServicingFundsResponse fetchServicingFunds() {
        return createRequest(SabadellConstants.Urls.FETCH_SERVICING_FUNDS)
                .get(ServicingFundsResponse.class);
    }

    public PensionPlansResponse fetchPensionPlans() {
        return createRequest(SabadellConstants.Urls.FETCH_PENSION_PLANS)
                .queryParam(
                        SabadellConstants.QueryParamPairs.NO_ERROR.getKey(),
                        SabadellConstants.QueryParamPairs.NO_ERROR.getValue())
                .queryParam(
                        SabadellConstants.QueryParamPairs.PAGE.getKey(),
                        SabadellConstants.QueryParamPairs.PAGE.getValue())
                .queryParam(
                        SabadellConstants.FetcherRequest.ITEMS_PER_PAGE,
                        SabadellConstants.PensionPlansRequest.ITEMS_PER_PAGE)
                .queryParam(
                        SabadellConstants.QueryParamPairs.ORDER_0.getKey(),
                        SabadellConstants.QueryParamPairs.ORDER_0.getValue())
                .get(PensionPlansResponse.class);
    }

    public SavingsResponse fetchSavings() {
        return createRequest(SabadellConstants.Urls.FETCH_SAVINGS).get(SavingsResponse.class);
    }

    public void logout() {
        createRequest(SabadellConstants.Urls.INITIATE_SESSION).delete();
    }

    public String fetchServicingFundsAccountDetails(ServicingFundsAccountDetailsRequest request) {
        return createRequest(SabadellConstants.Urls.FETCH_SERVICING_FUNDS_ACCOUNT_DETAILS)
                .post(String.class, request);
    }
}
