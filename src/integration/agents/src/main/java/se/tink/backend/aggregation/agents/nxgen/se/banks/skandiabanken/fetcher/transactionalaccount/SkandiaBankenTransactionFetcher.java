package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.rpc.FetchAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SkandiaBankenTransactionFetcher<T extends Account>
        implements TransactionPagePaginator<T> {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenTransactionFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(T account, int page) {
        // fetch booked transactions
        final FetchAccountTransactionsResponse transactionsResponse =
                apiClient.fetchAccountTransactions(
                        account.getApiIdentifier(), Integer.toString(page));
        // convert booked to tink transactions
        final List<Transaction> tinkTransactions =
                transactionsResponse.stream()
                        .map(te -> te.toTinkTransaction(false))
                        .collect(Collectors.toList());

        if (page == 1) {
            final List<Transaction> pendingTransactions = fetchPendingTransactions(account);
            tinkTransactions.addAll(pendingTransactions);
        }

        return PaginatorResponseImpl.create(
                tinkTransactions, transactionsResponse.size() == Fetcher.TRANSACTIONS_PER_BATCH);
    }

    private List<Transaction> fetchPendingTransactions(T account) {
        // fetch pending transactions
        final FetchAccountTransactionsResponse pendingTransactionsResponse =
                apiClient.fetchPendingAccountTransactions(account.getApiIdentifier());
        // convert pending to tink transactions
        return pendingTransactionsResponse.stream()
                .map(te -> te.toTinkTransaction(true))
                .collect(Collectors.toList());
    }
}
