package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsKeyPaginatorResponse implements TransactionKeyPaginatorResponse<String> {
    private TransactionsEntity transactions;

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public Collection<Transaction> toTinkTransactions() {
        return transactions.toTinkTransactions();
    }

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
