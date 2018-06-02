package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionIndexPaginator<A extends Account> {
    Collection<? extends Transaction> getTransactionsFor(A account, int numberOfTransactions, int startIndex);
}

