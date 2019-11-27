package se.tink.sa.framework.rest.model;

import java.util.Optional;

public interface PaginatorResponse {

    /**
     * It's at the paginator's discretion to decide when to stop if this returns `Optional.empty()`.
     *
     * @return true if the paginator knows or suspects there exist more transactions to fetch false
     *     if the paginator has evidence that there exist no more transactions to fetch
     *     Optional.empty() if the paginator suspects that there are no more transactions to fetch
     */
    Optional<Boolean> canFetchMore();
}
