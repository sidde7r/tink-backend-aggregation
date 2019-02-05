package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.util.Date;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionDatePaginator<A extends Account> {
    PaginatorResponse getTransactionsFor(A account, Date fromDate, Date toDate);
}
