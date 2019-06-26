package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

/**
 * Your transaction fetcher can return an instance of this when there is evidence that there are no
 * more transactions to fetch.
 */
public final class EmptyFinalPaginatorResponse implements PaginatorResponse {

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }
}
