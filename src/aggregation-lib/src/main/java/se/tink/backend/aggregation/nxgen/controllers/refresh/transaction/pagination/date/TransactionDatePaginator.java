package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionDatePaginator<A extends Account> {
    Collection<? extends Transaction> getTransactionsFor(A account, Date fromDate, Date toDate);
}
