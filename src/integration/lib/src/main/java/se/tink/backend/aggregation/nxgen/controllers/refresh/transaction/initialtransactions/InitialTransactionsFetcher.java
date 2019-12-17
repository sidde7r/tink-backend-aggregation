package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.initialtransactions;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface InitialTransactionsFetcher<A extends Account, R extends PaginatorResponse> {
    R fetchInitialTransactionsFor(A account);
}
