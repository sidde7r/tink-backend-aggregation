package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SebKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;
    private boolean pendingFetched = false;

    public SebKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        List<CreditCardTransaction> allTransactions = new ArrayList<>();

        final List<CreditCardTransaction> bookedTransactions =
                apiClient
                        .fetchTransactionsForContractId(
                                account.getBankIdentifier(), fromDate, toDate)
                        .getTransactions().stream()
                        .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                        .collect(Collectors.toList());

        allTransactions.addAll(getPendingTransactions(account));
        allTransactions.addAll(bookedTransactions);

        return PaginatorResponseImpl.create(allTransactions);
    }

    private List<CreditCardTransaction> getPendingTransactions(CreditCardAccount account) {
        if (pendingFetched) {
            return Collections.emptyList();
        }

        final List<CreditCardTransaction> pendingTransactions =
                apiClient.fetchReservationsForContractId(account.getBankIdentifier())
                        .getReservations().stream()
                        .map(transactionEntity -> transactionEntity.toTinkTransaction(true))
                        .collect(Collectors.toList());

        pendingFetched = true;

        return pendingTransactions;
    }
}
