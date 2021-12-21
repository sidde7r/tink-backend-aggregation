package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities.TransactionsPageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse extends TransactionsPageEntity
        implements TransactionKeyPaginatorResponse<String> {

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return this.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(super.isNextPageToken());
    }

    @Override
    public String nextKey() {
        return this.getNextPageToken();
    }
}
