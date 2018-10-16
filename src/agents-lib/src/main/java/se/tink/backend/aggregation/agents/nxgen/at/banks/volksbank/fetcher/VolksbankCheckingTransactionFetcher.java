package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class VolksbankCheckingTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final VolksbankApiClient apiClient;

    public VolksbankCheckingTransactionFetcher(
            VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return null;
    }
}
