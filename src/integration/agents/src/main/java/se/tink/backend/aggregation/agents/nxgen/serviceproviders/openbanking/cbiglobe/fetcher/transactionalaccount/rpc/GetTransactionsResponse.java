package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class GetTransactionsResponse implements PaginatorResponse {
    private TransactionsEntity transactions;
    private boolean pageRemaining;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        if (transactions == null) {
            return Collections.emptyList();
        }
        return transactions.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(pageRemaining);
    }

    public void setPageRemaining(boolean pageRemaining) {
        this.pageRemaining = pageRemaining;
    }
}
