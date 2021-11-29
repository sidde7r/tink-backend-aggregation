package se.tink.agent.sdk.fetching.transactions.index;

import java.util.Optional;
import se.tink.agent.sdk.fetching.transactions.PaginationResult;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.storage.SerializableReference;

public interface IndexPaginationFetcher extends TransactionsFetcher {
    Optional<IndexPaginationConfiguration> getConfiguration();

    PaginationResult fetchTransactionsFor(SerializableReference accountReference, int index);
}
