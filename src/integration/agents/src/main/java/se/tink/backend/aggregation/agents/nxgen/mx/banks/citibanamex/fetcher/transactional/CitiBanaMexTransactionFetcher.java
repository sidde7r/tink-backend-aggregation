package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc.PreviousTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CitiBanaMexTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final CitiBanaMexApiClient client;

    public CitiBanaMexTransactionFetcher(CitiBanaMexApiClient apiClient) {
        this.client = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        PreviousTransactionsResponse response =
                client.fetchPreviousTransactions(account.getApiIdentifier(), key);

        return new TransactionKeyPaginatorResponseImpl<>(
                response.toTinkTransactions(account.getExactBalance().getCurrencyCode()),
                getNextKey(response.getContinuityCode()));
    }

    private String getNextKey(String continuityCode) {
        return !Strings.isNullOrEmpty(continuityCode) ? continuityCode : null;
    }
}
