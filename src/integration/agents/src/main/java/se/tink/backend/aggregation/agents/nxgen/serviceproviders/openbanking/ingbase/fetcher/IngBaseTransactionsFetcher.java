package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngBaseTransactionsFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final IngBaseApiClient apiClient;
    private final Supplier<LocalDate> fromDateSupplier;

    public IngBaseTransactionsFetcher(
            IngBaseApiClient apiClient, Supplier<LocalDate> fromDateSupplier) {
        this.apiClient = apiClient;
        this.fromDateSupplier = fromDateSupplier;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        String transactionsUrl = account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL);

        if (transactionsUrl == null) {
            throw new IllegalStateException(
                    "Transactions link not present, can't fetch payment account transactions.");
        }

        if (Objects.isNull(key)) {
            final LocalDate fromDate = fromDateSupplier.get();
            final LocalDate toDate = LocalDate.now();
            return apiClient.fetchTransactions(transactionsUrl, fromDate, toDate);
        }
        return apiClient.fetchTransactionsPage(key);
    }
}
