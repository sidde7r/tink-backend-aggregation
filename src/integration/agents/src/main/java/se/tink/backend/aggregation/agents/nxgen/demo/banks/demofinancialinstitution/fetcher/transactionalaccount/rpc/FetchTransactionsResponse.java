package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    private String status;
    private String message;
    private List<TransactionEntity> transactions;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getTransactions().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
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
