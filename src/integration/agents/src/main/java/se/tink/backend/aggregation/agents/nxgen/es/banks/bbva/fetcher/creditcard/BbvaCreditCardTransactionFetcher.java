package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BbvaCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {
    private BbvaApiClient apiClient;

    public BbvaCreditCardTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        return apiClient.fetchCreditCardTransactions(account, key);
    }
}
