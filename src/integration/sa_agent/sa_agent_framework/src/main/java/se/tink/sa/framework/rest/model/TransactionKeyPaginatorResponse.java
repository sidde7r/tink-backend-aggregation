package se.tink.sa.framework.rest.model;

public interface TransactionKeyPaginatorResponse<T> extends PaginatorResponse {
    T nextKey();
}
