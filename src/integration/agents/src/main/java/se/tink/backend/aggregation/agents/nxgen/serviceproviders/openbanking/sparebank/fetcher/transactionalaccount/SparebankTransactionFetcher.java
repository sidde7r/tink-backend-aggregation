package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@RequiredArgsConstructor
public class SparebankTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final SparebankApiClient apiClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, @Nullable String key) {
        TransactionResponse transactionResponse;
        if (key == null) {
            transactionResponse = apiClient.fetchTransactions(account.getApiIdentifier());
        } else {
            transactionResponse = apiClient.fetchNextTransactions(key);
        }

        TransactionEntity transactions = transactionResponse.getTransactions();

        return new TransactionKeyPaginatorResponseImpl<>(
                transactions.toTinkTransactions(), transactions.getNext().orElse(null));
    }
}
