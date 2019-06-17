package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse implements TransactionKeyPaginatorResponse<String> {
    private TransactionEntity transactions;

    @Override
    public String nextKey() {
        return transactions.getNextLink();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.hasMore());
    }
}
