package se.tink.agent.sdk.fetching.transactions.key;

import se.tink.agent.sdk.fetching.transactions.PaginationResult;

public interface KeyPaginationResult<T> extends PaginationResult {
    T nextKey();
}
