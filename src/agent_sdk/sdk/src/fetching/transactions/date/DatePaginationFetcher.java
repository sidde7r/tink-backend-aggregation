package se.tink.agent.sdk.fetching.transactions.date;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.PaginationResult;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.storage.SerializableReference;

public interface DatePaginationFetcher extends TransactionsFetcher {
    Optional<DatePaginationConfiguration> getConfiguration();

    PaginationResult fetchTransactionsFor(
            SerializableReference accountReference, LocalDate fromDate, LocalDate toDate);
}
