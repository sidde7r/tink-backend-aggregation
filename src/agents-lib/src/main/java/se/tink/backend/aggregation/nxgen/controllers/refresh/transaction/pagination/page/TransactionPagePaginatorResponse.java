package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public interface TransactionPagePaginatorResponse {
    Collection<? extends Transaction> getTinkTransactions();
    boolean canFetchMore();
}
