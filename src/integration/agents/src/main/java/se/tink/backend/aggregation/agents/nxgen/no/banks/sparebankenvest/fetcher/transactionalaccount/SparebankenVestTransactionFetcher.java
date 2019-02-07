package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.DuePaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionsListResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.agents.rpc.Credentials;

public class SparebankenVestTransactionFetcher implements TransactionPagePaginator<TransactionalAccount>,
        UpcomingTransactionFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(SparebankenVestTransactionFetcher.class);

    private final SparebankenVestApiClient apiClient;
    private final Credentials credentials;

    private SparebankenVestTransactionFetcher(SparebankenVestApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public static SparebankenVestTransactionFetcher create(SparebankenVestApiClient apiClient,
            Credentials credentials) {
        return new SparebankenVestTransactionFetcher(apiClient, credentials);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        String rangeString = getRangeString(page);
        TransactionsListResponse transactionsList = apiClient.fetchTransactions(account.getBankIdentifier(), rangeString);

        List<Transaction> tinkTransactions = transactionsList.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        return PaginatorResponseImpl.create(tinkTransactions,
                transactionsList.size() >= SparebankenVestConstants.PagePagination.MAX_TRANSACTIONS_IN_BATCH);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        DuePaymentsResponse duePaymentsResponse = apiClient.fetchUpcomingTransactions();

        return duePaymentsResponse.getUpcomingTransactions();
    }

    private String getRangeString(int page) {
        int startIndex = page * SparebankenVestConstants.PagePagination.MAX_TRANSACTIONS_IN_BATCH;
        int endIndex = (page + 1) * SparebankenVestConstants.PagePagination.MAX_TRANSACTIONS_IN_BATCH - 1;

        return new StringBuilder()
                .append(SparebankenVestConstants.Headers.RANGE_ITEMS)
                .append(startIndex)
                .append(SparebankenVestConstants.Headers.RANGE_DASH)
                .append(endIndex)
                .toString();
    }
}
