package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class IngTransactionPageResponse implements TransactionKeyPaginatorResponse<String> {

    private final List<Transaction> transactions;
    private final String nextKey;

    public IngTransactionPageResponse(List<Transaction> transactions, String nextKey) {
        this.transactions = transactions;
        this.nextKey = nextKey;
    }

    @Override
    public String nextKey() {
        return nextKey;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextKey != null);
    }
}
