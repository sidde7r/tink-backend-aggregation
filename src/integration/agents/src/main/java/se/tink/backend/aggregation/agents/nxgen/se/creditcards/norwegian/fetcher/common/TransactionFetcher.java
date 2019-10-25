package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionFetcher {

    public static PaginatorResponse fetchTransactionsFor(
            NorwegianApiClient apiClient, String account, Date from, Date to) {
        List<Transaction> transactions =
                apiClient.fetchTransactions(account, from, to).stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }
}
