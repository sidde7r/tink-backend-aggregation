package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransfersResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@JsonObject
public class SbabSavingsAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {
    private final SbabApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SbabSavingsAccountFetcher(SbabApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.listAccounts().getAccounts().stream()
                .filter(AccountEntity::isSavingsAccount)
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        return Optional.of(apiClient.listTransfers(account.getAccountNumber()))
                .orElse(new TransfersResponse());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final boolean shouldFetchMore =
                persistentStorage
                        .get(StorageKey.ENVIRONMENT, Environment.class)
                        // Only run once for sandbox because of no pagination
                        .map(env -> env != Environment.SANDBOX)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No SBAB environment is set in persistent storage."));

        return Optional.of(apiClient.listTransfers(account.getAccountNumber(), fromDate, toDate))
                .orElse(new TransfersResponse())
                .withShouldFetchMore(shouldFetchMore);
    }
}
