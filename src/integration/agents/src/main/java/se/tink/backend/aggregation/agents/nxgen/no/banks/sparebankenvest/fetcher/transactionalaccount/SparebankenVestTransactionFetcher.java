package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.Payload;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.DuePaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc.TransactionsListResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
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
    private static final AggregationLogger LOGGER =
            new AggregationLogger(SparebankenVestTransactionFetcher.class);

    private final SparebankenVestApiClient apiClient;
    private final Credentials credentials;

    private static final DateFormat DATEFROMAT = new SimpleDateFormat("yyyy-MM-dd");

    private SparebankenVestTransactionFetcher(
            SparebankenVestApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public static SparebankenVestTransactionFetcher create(
            SparebankenVestApiClient apiClient, Credentials credentials) {
        return new SparebankenVestTransactionFetcher(apiClient, credentials);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date from, Date to) {
        String fromFormatted = DATEFROMAT.format(from);
        String toFormatted = DATEFROMAT.format(to);

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
        DuePaymentsResponse duePaymentsResponse = apiClient.fetchUpcomingTransactions();

        return duePaymentsResponse.getUpcomingTransactions();
    }
}
