package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SEBKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SEBKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        List<Transaction> tinkTransactions = new ArrayList<>();

        List<Transaction> reservations = apiClient.fetchReservations(account.getApiIdentifier())
                .getTinkTransactions();

        tinkTransactions.addAll(
                handleReservations(fromDate, toDate, reservations));

        tinkTransactions.addAll(
                apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate)
                        .getTinkTranscations()
        );

        return PaginatorResponseImpl.create(tinkTransactions);
    }

    private static List<Transaction> handleReservations(Date fromDate, Date toDate, List<Transaction> reservations) {
        return reservations.stream()
                .filter(transaction -> transaction.getDate().after(fromDate) && transaction.getDate().before(toDate))
                .collect(Collectors.toList());
    }
}
