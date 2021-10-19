package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.hybrid;

import java.time.OffsetDateTime;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

/**
 * Hybrid paginator interface that allows to:
 *
 * <p>- start fetching transactions within specific date range
 *
 * <p>- continue fetch by specific key
 *
 * @param <ACCOUNT> specific Tink's account model for which transactions will be fetched, e.g {@link
 *     se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount},
 * @param <KEY> type of argument that will be used as key
 */
public interface TransactionOffsetDateTimeKeyPaginator<ACCOUNT extends Account, KEY>
        extends TransactionKeyPaginator<ACCOUNT, KEY> {

    /**
     * @return paginator response that allows for providing next key - since fetching within date
     *     range will be executed first, response needs to provide key to enable further fetching by
     *     key
     */
    TransactionKeyPaginatorResponse<KEY> getTransactionsFor(
            ACCOUNT account, OffsetDateTime from, OffsetDateTime to);
}
