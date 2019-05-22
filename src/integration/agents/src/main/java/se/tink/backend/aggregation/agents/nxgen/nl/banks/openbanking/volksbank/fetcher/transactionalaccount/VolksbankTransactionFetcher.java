package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class VolksbankTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final VolksbankApiClient apiClient;

    public VolksbankTransactionFetcher(final VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            final TransactionalAccount account, final String key) {

        final TransactionsEntity response =
                apiClient
                        .readTransactions(account, apiClient.getUtils().splitURLQuery(key))
                        .getTransactions();
        return response;
    }
}
