package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.Month;
import java.time.Year;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionMonthPaginator<A extends Account> {
    PaginatorResponse getTransactionsFor(A account, Year year, Month month);
}
