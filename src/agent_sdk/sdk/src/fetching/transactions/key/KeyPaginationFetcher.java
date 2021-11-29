package se.tink.agent.sdk.fetching.transactions.key;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;
import se.tink.agent.sdk.storage.SerializableReference;

public interface KeyPaginationFetcher<T> extends TransactionsFetcher {
    Optional<KeyPaginationConfiguration> getConfiguration();

    KeyPaginationResult<T> fetchTransactionsFor(
            SerializableReference accountReference, @Nullable T key);
}
