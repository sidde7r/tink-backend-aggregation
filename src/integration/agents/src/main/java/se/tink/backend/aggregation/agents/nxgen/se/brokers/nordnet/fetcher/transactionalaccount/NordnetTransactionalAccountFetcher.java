package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class NordnetTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final NordnetApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordnetTransactionalAccountFetcher(
            NordnetApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accounts = apiClient.fetchAccounts();
        sessionStorage.put(StorageKeys.ACCOUNTS, accounts);

        return accounts.stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity account) {

        ExactCurrencyAmount balance =
                apiClient.fetchAccountInfo(account.getAccountId()).stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Could not fetch balance"))
                        .getAccountSum();

        return account.toTinkAccount(balance);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
