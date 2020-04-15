package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DkbTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final DkbApiClient apiClient;

    public DkbTransactionalAccountFetcher(DkbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .map(this::toTinkAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccountWithBalance(AccountEntity accountEntity) {
        return accountEntity.toTinkAccount(getAccountBalance(accountEntity));
    }

    private ExactCurrencyAmount getAccountBalance(AccountEntity accountEntity) {
        return apiClient.getBalances(accountEntity.getResourceId()).getBalance();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final GetTransactionsResponse transactions =
                apiClient.getTransactions(account, fromDate, toDate);

        return PaginatorResponseImpl.create(transactions.toTinkTransactions());
    }
}
