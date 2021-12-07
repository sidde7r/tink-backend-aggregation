package se.tink.agent.agents.example.fetcher;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.PaginationResult;
import se.tink.agent.sdk.fetching.transactions.date.DatePaginationConfiguration;
import se.tink.agent.sdk.fetching.transactions.date.DatePaginationFetcher;
import se.tink.agent.sdk.storage.Reference;

public class ExampleSavingsTransactionsFetcher implements DatePaginationFetcher {
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
