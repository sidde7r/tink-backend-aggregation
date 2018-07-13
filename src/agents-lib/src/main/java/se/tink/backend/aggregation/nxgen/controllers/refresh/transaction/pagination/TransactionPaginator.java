package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionPaginator<A extends Account> {
    Collection<? extends Transaction> fetchTransactionsFor(A account);
    boolean canFetchMoreFor(A account);
}
