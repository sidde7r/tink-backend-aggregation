package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NorwegianPaginatorResponse implements PaginatorResponse {

    private final List<Transaction> transactions;

    public NorwegianPaginatorResponse(List<Transaction> transactions) {
        this.transactions = Objects.requireNonNull(transactions);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
