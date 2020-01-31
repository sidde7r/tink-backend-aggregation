package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.DateUtils;

public class CreditAgricoleBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final CreditAgricoleBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public CreditAgricoleBaseTransactionalAccountFetcher(
            CreditAgricoleBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        if (getAccountsResponse.areConsentsNecessary()) {
            apiClient.putConsents(getAccountsResponse.getListOfNecessaryConsents());
            getAccountsResponse = apiClient.getAccounts();
        }

        return getAccountsResponse.toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return isInitialFetch()
                ? getAllTransactions(account, fromDate, toDate)
                : get90DaysTransactions(account, toDate);
    }

    private PaginatorResponse getAllTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return PaginatorResponseImpl.create(
                apiClient
                        .getTransactions(account.getApiIdentifier(), fromDate, toDate)
                        .getTinkTransactions());
    }

    private PaginatorResponse get90DaysTransactions(TransactionalAccount account, Date toDate) {
        return PaginatorResponseImpl.create(
                apiClient
                        .getTransactions(
                                account.getApiIdentifier(), DateUtils.addDays(toDate, -90), toDate)
                        .getTinkTransactions(),
                false);
    }

    private boolean isInitialFetch() {
        return persistentStorage
                .get(StorageKeys.IS_INITIAL_FETCH, Boolean.class)
                .orElse(Boolean.FALSE);
    }
}
