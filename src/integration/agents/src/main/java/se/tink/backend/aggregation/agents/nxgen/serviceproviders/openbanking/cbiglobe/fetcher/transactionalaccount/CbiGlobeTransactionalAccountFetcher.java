package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String queryValue;
    private static final int TRANSACTIONS_DAYS_BACK = 90;

    private CbiGlobeTransactionalAccountFetcher(
            CbiGlobeApiClient apiClient, PersistentStorage persistentStorage, String queryValue) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.queryValue = queryValue;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        persistentStorage.get(StorageKeys.ACCOUNTS), GetAccountsResponse.class);
        return getAccountsResponse.getAccounts().stream()
                .filter(acc -> !acc.isEmptyAccountObject())
                .map(acc -> acc.toTinkAccount(apiClient.getBalances(acc.getResourceId())))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static CbiGlobeTransactionalAccountFetcher createFromBoth(
            CbiGlobeApiClient apiClient, PersistentStorage persistentStorage) {
        return new CbiGlobeTransactionalAccountFetcher(
                apiClient, persistentStorage, QueryValues.BOTH);
    }

    public static CbiGlobeTransactionalAccountFetcher createFromBooked(
            CbiGlobeApiClient apiClient, PersistentStorage persistentStorage) {
        return new CbiGlobeTransactionalAccountFetcher(
                apiClient, persistentStorage, QueryValues.BOOKED);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(TRANSACTIONS_DAYS_BACK);
        return apiClient.getTransactions(
                account.getApiIdentifier(), fromDate, toDate, this.queryValue, page);
    }
}
