package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class PayPalTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final PayPalApiClient apiClient;

    public PayPalTransactionFetcher(PayPalApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(() -> apiClient.getTransactions());
    }
}
