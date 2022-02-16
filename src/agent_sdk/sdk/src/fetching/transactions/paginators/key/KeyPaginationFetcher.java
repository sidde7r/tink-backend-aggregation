package se.tink.agent.sdk.fetching.transactions.paginators.key;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.storage.Reference;

public interface KeyPaginationFetcher<T> extends TransactionsFetcher {
    Optional<KeyPaginationConfiguration> getConfiguration();

    KeyPaginationResult<T> fetchTransactionsFor(Reference accountReference, @Nullable T key);
}
