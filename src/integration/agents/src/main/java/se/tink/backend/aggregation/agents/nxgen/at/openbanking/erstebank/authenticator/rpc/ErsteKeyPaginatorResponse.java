package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.entities.ErsteTransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class ErsteKeyPaginatorResponse implements TransactionKeyPaginatorResponse<String> {

    private ErsteTransactionsEntity transactions;

    public ErsteTransactionsEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(ErsteTransactionsEntity transactions) {
        this.transactions = transactions;
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
