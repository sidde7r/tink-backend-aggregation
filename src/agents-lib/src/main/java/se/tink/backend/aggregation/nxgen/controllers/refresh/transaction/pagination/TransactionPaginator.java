package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionPaginator<A extends Account> {
    PaginatorResponse fetchTransactionsFor(A account);
}
