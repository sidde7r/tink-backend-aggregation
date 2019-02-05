package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionIndexPaginator<A extends Account> {
    PaginatorResponse getTransactionsFor(A account, int numberOfTransactions, int startIndex);
}

