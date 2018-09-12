package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class LaCaixaCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final LaCaixaApiClient apiClient;

    public LaCaixaCreditCardFetcher(LaCaixaApiClient bankApi) {
        this.apiClient = bankApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCards().toTinkCards();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        // Pagination state is maintained on the server. We should only indicate if this is new/first request or not.
        // The response contains a boolean that indicates if there is more data to fetch or not.
        return apiClient.fetchCardTransactions(account.getBankIdentifier(), page == 0);
    }
}
