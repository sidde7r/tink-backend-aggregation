package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions.TransactionsBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsKeyPaginatorBaseResponse
        implements TransactionKeyPaginatorResponse<String> {
    private TransactionsBaseEntity transactions;

    public TransactionsBaseEntity getTransactions() {
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
