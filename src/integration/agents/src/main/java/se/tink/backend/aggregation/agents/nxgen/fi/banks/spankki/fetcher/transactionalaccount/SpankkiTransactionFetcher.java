package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities.ReservationsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc.ReservationsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SpankkiTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private static final int ONE_WEEK_AGO_IN_DAYS = -7;
    private final SpankkiApiClient apiClient;

    public SpankkiTransactionFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        List<Transaction> transactions = new ArrayList<>();

        if (shouldIncludeReservations(toDate)) {
            ReservationsResponse reservationsResponse = apiClient.fetchReservations(account.getBankIdentifier());
            if (reservationsResponse.getReservations() != null) {
                transactions.addAll(reservationsResponse.getReservations().stream()
                        .map(ReservationsEntity::toTinkTransaction)
                        .collect(Collectors.toList()));
            }
        }
        GetTransactionsResponse transactionsResponse = apiClient
                .fetchTransactions(account.getBankIdentifier(), formatDate(fromDate), formatDate(toDate));
        if (transactionsResponse.getTransactions() != null) {
            transactions.addAll(transactionsResponse.getTransactions().stream()
                    .map(TransactionsEntity::toTinkTransaction)
                    .collect(Collectors.toList()));
        }

        return PaginatorResponseImpl.create(transactions);
    }

    // only fetch reservations when asking for most current transactions
    private boolean shouldIncludeReservations(Date toDate) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, ONE_WEEK_AGO_IN_DAYS);
        return toDate.after(c.getTime());
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }

        return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(date);
    }
}
