package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.LocalDate;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface KeyWithInitiDateFromFetcher<A extends Account, T>
        extends TransactionKeyPaginator<A, T> {

    TransactionKeyPaginatorResponse<T> fetchTransactionsFor(A account, LocalDate dateFrom);

    LocalDate minimalFromDate();
}
