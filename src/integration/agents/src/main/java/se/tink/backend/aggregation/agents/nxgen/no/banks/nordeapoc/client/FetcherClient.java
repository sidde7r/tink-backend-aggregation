package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.QueryParamKeys;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.QueryParamValues;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.UriParams;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.Urls;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.investment.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@AllArgsConstructor
public class FetcherClient {

    private BaseClient baseClient;

    public AccountsResponse fetchAccounts() {
        return baseClient.baseAuthorizedRequest(Urls.FETCH_ACCOUNTS).get(AccountsResponse.class);
    }

    public TransactionsResponse fetchAccountTransactions(
            String accountId, String productCode, String continuationKey) {

        RequestBuilder request =
                baseClient.baseAuthorizedRequest(
                        new URL(Urls.FETCH_ACCOUNT_TRANSACTIONS)
                                .parameter(UriParams.URI_ACCOUNT_ID, accountId));
        request.queryParam(QueryParamKeys.PRODUCT_CODE, productCode);
        if (continuationKey != null) {
            request.queryParam(QueryParamKeys.CONTINUATION_KEY, continuationKey);
        }
        return request.get(TransactionsResponse.class);
    }

    public CreditCardsResponse fetchCreditCards() {
        return baseClient.baseAuthorizedRequest(Urls.FETCH_CARDS).get(CreditCardsResponse.class);
    }

    public CreditCardDetailsResponse fetchCreditCardDetails(String cardId) {
        return baseClient
                .baseAuthorizedRequest(
                        new URL(Urls.FETCH_CARD_DETAILS).parameter(UriParams.URI_CARD_ID, cardId))
                .get(CreditCardDetailsResponse.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String cardId, int page) {
        return baseClient
                .baseAuthorizedRequest(
                        new URL(Urls.FETCH_CARD_TRANSACTIONS)
                                .parameter(UriParams.URI_CARD_ID, cardId))
                .queryParam(QueryParamKeys.PAGE, String.valueOf(page))
                .queryParam(QueryParamKeys.PAGE_SIZE, QueryParamValues.PAGE_SIZE)
                .get(CreditCardTransactionsResponse.class);
    }

    public InvestmentsResponse fetchInvestments() {
        return baseClient
                .baseAuthorizedRequest(Urls.FETCH_INVESTMENTS)
                .queryParam(QueryParamKeys.TYPE, QueryParamValues.TYPE)
                .get(InvestmentsResponse.class);
    }

    public IdentityDataResponse fetchIdentityData() {
        return baseClient
                .baseAuthorizedRequest(Urls.FETCH_IDENTITY_DATA)
                .get(IdentityDataResponse.class);
    }

    public LoansResponse fetchLoans() {
        return baseClient.baseAuthorizedRequest(Urls.FETCH_LOANS).get(LoansResponse.class);
    }

    public LoanDetailsResponse fetchLoanDetails(String loanId) {
        return baseClient
                .baseAuthorizedRequest(
                        new URL(Urls.FETCH_LOAN_DETAILS).parameter(UriParams.URI_LOAN_ID, loanId))
                .get(LoanDetailsResponse.class);
    }
}
