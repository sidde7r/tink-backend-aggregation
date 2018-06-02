package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionMonthPaginator {
    Collection<Transaction> getTransactionsFor(Account account, Year year, Month month);
}
