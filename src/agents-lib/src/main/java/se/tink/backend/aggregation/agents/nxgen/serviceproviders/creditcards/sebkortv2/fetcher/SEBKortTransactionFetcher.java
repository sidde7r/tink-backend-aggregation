package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SEBKortTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SEBKortTransactionFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Date fromDate, Date toDate) {
        List<Transaction> tinkTransactions = new ArrayList<>();

        tinkTransactions.addAll(
                apiClient.fetchReservations(account.getBankIdentifier(), fromDate, toDate)
                        .getTinkTransactions()
        );

        tinkTransactions.addAll(
                apiClient.fetchTransactions(account.getBankIdentifier(), fromDate, toDate)
                        .getTinkTranscations()
        );

        return PaginatorResponseImpl.create(tinkTransactions);
    }
}
