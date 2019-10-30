package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MontepioTransactionalAccountsFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private final MontepioApiClient client;

    public MontepioTransactionalAccountsFetcher(final MontepioApiClient client) {
        this.client = requireNonNull(client);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getResultEntity().getAccounts().orElseGet(ArrayList::new)
                .stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        LocalDate to = LocalDate.now();
        LocalDate from = LocalDate.now().minusMonths(6);

        FetchTransactionsResponse response = client.fetchTransactions(account, page, from, to);
        List<Transaction> transactions =
                response.getResultEntity().getTransactions().orElseGet(Collections::emptyList)
                        .stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());
        return PaginatorResponseImpl.create(
                transactions, response.getResultEntity().hasMorePages());
    }
}
