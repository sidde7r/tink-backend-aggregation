package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;

public interface TransactionKeyPaginatorResponse<T> extends PaginatorResponse {
    T nextKey();
}
