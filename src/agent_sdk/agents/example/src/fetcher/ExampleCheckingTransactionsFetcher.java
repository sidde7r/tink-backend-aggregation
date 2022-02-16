package se.tink.agent.agents.example.fetcher;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.pagination_result.PaginationResult;
import se.tink.agent.sdk.fetching.transactions.paginators.date.DatePaginationConfiguration;
import se.tink.agent.sdk.fetching.transactions.paginators.date.DatePaginationFetcher;
import se.tink.agent.sdk.storage.Reference;

public class ExampleCheckingTransactionsFetcher implements DatePaginationFetcher {
    @Override
    public Optional<DatePaginationConfiguration> getConfiguration() {
        return Optional.empty();
    }

    @Override
    public PaginationResult fetchTransactionsFor(
            Reference accountReference, LocalDate fromDate, LocalDate toDate) {

        return null;
    }
}
