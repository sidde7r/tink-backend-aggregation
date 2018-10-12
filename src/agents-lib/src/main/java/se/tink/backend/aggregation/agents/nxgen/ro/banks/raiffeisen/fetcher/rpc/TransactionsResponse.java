package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity.TransactionListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {

    private TransactionListEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

}
