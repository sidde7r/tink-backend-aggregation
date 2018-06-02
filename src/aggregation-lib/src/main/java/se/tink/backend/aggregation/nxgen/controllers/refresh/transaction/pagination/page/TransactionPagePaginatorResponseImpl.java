package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionPagePaginatorResponseImpl implements TransactionPagePaginatorResponse {

    private Collection<Transaction> transactions = new ArrayList<>();
    private boolean canFetchMore = false;

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void setTransactions(Collection<Transaction> transactionsList) {
        transactions = transactionsList;
    }

    public void setCanFetchMore(boolean canFetchMore) {
        this.canFetchMore = canFetchMore;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public boolean canFetchMore() {
        return canFetchMore;
    }
}
