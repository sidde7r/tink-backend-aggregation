package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.initialtransactions;

import java.time.LocalDate;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface InitialTransactionsFromCertainDateFetcher<
        A extends Account, R extends PaginatorResponse> {
    R fetchInitialTransactionsFor(A account, LocalDate fromDate);
}
