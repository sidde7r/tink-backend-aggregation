package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionPaginator<A extends Account> {
    // In case the paginator keeps a state per account.
    // This method will be called for each new account to allow the paginator to reset the state.
    void resetState();
    PaginatorResponse fetchTransactionsFor(A account);
}
