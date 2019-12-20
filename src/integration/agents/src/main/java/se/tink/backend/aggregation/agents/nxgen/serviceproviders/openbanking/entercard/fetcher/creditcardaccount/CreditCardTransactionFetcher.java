package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.creditcardaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionKey;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, TransactionKey> {

    private final EnterCardApiClient apiClient;

    public CreditCardTransactionFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionKey> getTransactionsFor(
            CreditCardAccount account, TransactionKey key) {
        if (key == null) {
            key = new TransactionKey(0);
        }
        return apiClient.fetchCreditCardTransactions(account, key);
    }
}
