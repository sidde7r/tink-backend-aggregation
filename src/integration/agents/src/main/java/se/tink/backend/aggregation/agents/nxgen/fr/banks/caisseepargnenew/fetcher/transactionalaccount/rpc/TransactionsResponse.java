package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity.TransactionsResultsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsResponse extends GenericResponse<TransactionsResultsEntity>
        implements TransactionKeyPaginatorResponse<String> {

    @Override
    public String nextKey() {
        return results.nextKey();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return results.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return results.canFetchMore();
    }
}
