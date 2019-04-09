package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc.TransfersResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SbabSavingsAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {
    private final SbabApiClient apiClient;

    public SbabSavingsAccountFetcher(SbabApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.listAccounts().getAccounts().stream()
                .filter(AccountEntity::isSavingsAccount)
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final boolean shouldFetchMore =
                apiClient.getConfiguration().getEnvironment() != Environment.SANDBOX;

        return Optional.of(apiClient.listTransfers(account.getAccountNumber(), fromDate, toDate))
                .orElse(new TransfersResponse())
                .withShouldFetchMore(shouldFetchMore);
    }
}
