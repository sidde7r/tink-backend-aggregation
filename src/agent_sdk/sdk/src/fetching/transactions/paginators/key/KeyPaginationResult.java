package se.tink.agent.sdk.fetching.transactions.paginators.key;

import se.tink.agent.sdk.fetching.transactions.pagination_result.PaginationResult;

public interface KeyPaginationResult<T> extends PaginationResult {
    T nextKey();
}
