package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class LuminorTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private final LuminorApiClient apiClient;

    public LuminorTransactionsFetcher(LuminorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        LocalDate toDate = LocalDate.now().minusDays(90);
        LocalDate fromDate = LocalDate.now();

        TransactionsResponse transactionsResponse =
                apiClient.getTransactions(account.getAccountNumber(), toDate, fromDate);

        return transactionsResponse.getTinkTransactions();
    }
}
