package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SebKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SebKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        final List<TransactionEntity> reservations =
                apiClient
                        .fetchReservations(account.getBankIdentifier(), fromDate, toDate)
                        .getReservations();
        final List<TransactionEntity> transactions =
                apiClient
                        .fetchTransactions(account.getBankIdentifier(), fromDate, toDate)
                        .getTransactions();

        final Collection<? extends Transaction> collect =
                Stream.of(reservations, transactions)
                        .flatMap(Collection::stream)
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(collect);
    }
}
