package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class ForwardPaginationByAccountTypeController<A extends Account> implements TransactionPaginator<A> {
    private final TransactionPaginator<A> defaultPaginator;
    private final Map<AccountTypes, TransactionPaginator<A>> paginatorsByAccountType;

    public ForwardPaginationByAccountTypeController(TransactionPaginator<A> defaultPaginator,
            Map<AccountTypes, TransactionPaginator<A>> paginatorsByAccountType) {
        this.defaultPaginator = Preconditions.checkNotNull(defaultPaginator);
        this.paginatorsByAccountType = Preconditions.checkNotNull(paginatorsByAccountType);

        Preconditions.checkArgument(!paginatorsByAccountType.isEmpty() &&
                !paginatorsByAccountType.containsValue(defaultPaginator));
    }

    @Override
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        return getPaginatorFor(account).fetchTransactionsFor(account);
    }

    @Override
    public boolean canFetchMoreFor(A account) {
        return getPaginatorFor(account).canFetchMoreFor(account);
    }

    private TransactionPaginator<A> getPaginatorFor(A account) {
        return paginatorsByAccountType.getOrDefault(account.getType(), defaultPaginator);
    }
}
