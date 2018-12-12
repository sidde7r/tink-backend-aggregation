package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

/**
 * All paginators MUST return this or an extended version of this interface.
 */
public interface PaginatorResponse {
    Collection<? extends Transaction> getTinkTransactions();

    /**
     * It's at the paginator's discretion to decide when to stop if this returns `Optional.empty()`.
     *
     * @return true if the paginator knows or suspects there exist more transactions to fetch
     *         false if the paginator has evidence that there exist no more transactions to fetch
     *         Optional.empty() if the paginator suspects that there are no more transactions to fetch
     */
    Optional<Boolean> canFetchMore();
}
