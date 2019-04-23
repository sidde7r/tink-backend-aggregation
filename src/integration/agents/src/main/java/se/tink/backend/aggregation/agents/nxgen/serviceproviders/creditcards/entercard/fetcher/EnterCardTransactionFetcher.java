package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class EnterCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {

    private EnterCardApiClient apiClient;

    public EnterCardTransactionFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return apiClient.fetchTransactions(account.getAccountNumber(), page, 50);
    }
}
