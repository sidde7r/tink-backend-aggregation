package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.Payload;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.TransactionsListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class SparebankenVestTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final SparebankenVestApiClient apiClient;

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SparebankenVestTransactionFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestTransactionFetcher create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestTransactionFetcher(apiClient);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date from, Date to) {
        String fromFormatted = dateFormat.format(from);
        String toFormatted = dateFormat.format(to);

        TransactionsListResponse transactionsList =
                apiClient.fetchTransactions(account.getAccountNumber(), fromFormatted, toFormatted);

        Payload payload = transactionsList.getPayload();

        List<Transaction> tinkTransactions =
                payload.getTransaksjoner().stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(tinkTransactions);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        // Temporarily disable upcoming transactions as it blocks fetching transaction list.
        // Should be fixed in ITE-1553
        // DuePaymentsResponse duePaymentsResponse = apiClient.fetchUpcomingTransactions()

        return Collections.emptyList();
    }
}
