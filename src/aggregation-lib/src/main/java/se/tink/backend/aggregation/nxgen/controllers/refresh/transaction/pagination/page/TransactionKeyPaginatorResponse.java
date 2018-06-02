package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionKeyPaginatorResponse<T> {
    Collection<? extends Transaction> getTinkTransactions();
    boolean hasNext();
    T nextKey();
}
