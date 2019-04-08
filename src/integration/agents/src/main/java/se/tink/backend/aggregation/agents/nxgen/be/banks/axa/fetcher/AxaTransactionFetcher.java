package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public final class AxaTransactionFetcher implements TransactionFetcher<TransactionalAccount> {
    private final AxaStorage storage;
    private final AxaApiClient apiClient;

    public AxaTransactionFetcher(final AxaApiClient apiClient, final AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(final TransactionalAccount account) {
        final int customerId = storage.getCustomerId().orElseThrow(IllegalStateException::new);
        final String accessToken = storage.getAccessToken().orElseThrow(IllegalStateException::new);
        final String locale = storage.getLanguage().orElse(""); // Dutch is the fallback

        final GetTransactionsResponse response =
                apiClient.postGetTransactions(
                        customerId, accessToken, account.getAccountNumber(), locale);

        return response.getTransactions();
    }
}
