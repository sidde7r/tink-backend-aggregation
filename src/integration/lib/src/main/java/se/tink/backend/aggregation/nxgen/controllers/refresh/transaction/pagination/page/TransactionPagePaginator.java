package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionPagePaginator<A extends Account> {
    PaginatorResponse getTransactionsFor(A account, int page);
}
