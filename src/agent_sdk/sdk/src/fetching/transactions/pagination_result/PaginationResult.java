package se.tink.agent.sdk.fetching.transactions.pagination_result;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface PaginationResult {

    @SuppressWarnings("java:S1452")
    List<? extends Transaction> getTinkTransactions();

    /**
     * It's at the paginator's discretion to decide when to stop if this returns `Optional.empty()`.
     *
     * @return true if the paginator knows or suspects there exist more transactions to fetch false
     *     if the paginator has evidence that there exist no more transactions to fetch
     *     Optional.empty() if the paginator suspects that there are no more transactions to fetch
     */
    Optional<Boolean> canFetchMore();
}
