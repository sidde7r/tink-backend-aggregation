package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public interface TransactionKeyPaginator<A extends Account, T> {
    TransactionKeyPaginatorResponse<T> getTransactionsFor(A account, @Nullable T key);
}
