package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class IngBaseCardTransactionsFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {

    private final IngBaseApiClient apiClient;
    private final Supplier<LocalDate> fromDateSupplier;

    public IngBaseCardTransactionsFetcher(
            IngBaseApiClient apiClient, Supplier<LocalDate> fromDateSupplier) {
        this.apiClient = apiClient;
        this.fromDateSupplier = fromDateSupplier;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        if (Objects.isNull(key)) {
            final LocalDate fromDate = fromDateSupplier.get();
            final LocalDate toDate = LocalDate.now();
            return apiClient.fetchCardTransactions(
                    account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL),
                    fromDate,
                    toDate);
        }
        return apiClient.fetchCardTransactionsPage(key);
    }
}
