package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class FetchTransactionsResponse extends ArrayList<TransactionEntity>
        implements TransactionKeyPaginatorResponse<URL> {

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return this.stream().map(TransactionEntity::toTinkTransaction).collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // Spark Java and JDBI doesn't have any built-in pagination
        return Optional.of(false);
    }

    @Override
    public URL nextKey() {
        return null;
    }
}
