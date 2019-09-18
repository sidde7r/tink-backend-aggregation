package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {
    private AccountEntity account;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // they don't support pagination. All the transactions are returned in one response
        return Optional.of(false);
    }

    public void setBookedTransactions(List<TransactionEntity> bookedTransactions) {
        transactions.setBookedTransactions(bookedTransactions);
    }

    public List<TransactionEntity> getBookedTransactoins() {
        return transactions.getBooked();
    }
}
