package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class UpcomingTransactionFetcher
        implements se.tink.backend.aggregation.nxgen.controllers.refresh.transaction
                        .UpcomingTransactionFetcher<
                TransactionalAccount> {
    private final SebApiClient apiClient;

    public UpcomingTransactionFetcher(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        final String customerId = account.getFromTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID);
        final String accountId = account.getApiIdentifier();
        final Response response = apiClient.fetchUpcomingTransactions(customerId);

        return response.getUpcomingTransactions().stream()
                .filter(transaction -> accountId.equals(transaction.getAccountNumber()))
                .map(UpcomingTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
