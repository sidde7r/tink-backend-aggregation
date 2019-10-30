package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils.calculateFromDate;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final CbiGlobeApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String queryValue;

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

        Date fromDate = calculateFromDate(new Date());

        return getAccountsResponse.getAccounts().stream()
                .map(
                        acc ->
                                acc.toTinkAccount(
                                        apiClient.getTransactionsBalances(
                                                acc.getResourceId(),
                                                fromDate,
                                                new Date(),
                                                this.queryValue)))
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactionsFromTempMap(account.getApiIdentifier());
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
}
