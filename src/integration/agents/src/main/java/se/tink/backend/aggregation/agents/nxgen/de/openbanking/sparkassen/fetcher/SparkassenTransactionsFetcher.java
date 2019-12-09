package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SparkassenTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private final SparkassenApiClient apiClient;

    public SparkassenTransactionsFetcher(SparkassenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return Optional.ofNullable(apiClient.fetchTransactions(account.getApiIdentifier()))
                .orElseThrow(IllegalStateException::new)
                .toTinkTransactions();
    }
}
