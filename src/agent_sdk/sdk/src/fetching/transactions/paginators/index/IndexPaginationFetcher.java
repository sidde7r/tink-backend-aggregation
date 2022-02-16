package se.tink.agent.sdk.fetching.transactions.paginators.index;

import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.fetching.transactions.pagination_result.PaginationResult;
import se.tink.agent.sdk.storage.Reference;

public interface IndexPaginationFetcher extends TransactionsFetcher {
    Optional<IndexPaginationConfiguration> getConfiguration();

    PaginationResult fetchTransactionsFor(Reference accountReference, int index);
}
