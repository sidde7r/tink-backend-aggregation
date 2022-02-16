package se.tink.agent.sdk.fetching.transactions.paginators.date;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.fetching.transactions.pagination_result.PaginationResult;
import se.tink.agent.sdk.storage.Reference;

public interface DatePaginationFetcher extends TransactionsFetcher {
    Optional<DatePaginationConfiguration> getConfiguration();

    PaginationResult fetchTransactionsFor(
            Reference accountReference, LocalDate fromDate, LocalDate toDate);
}
