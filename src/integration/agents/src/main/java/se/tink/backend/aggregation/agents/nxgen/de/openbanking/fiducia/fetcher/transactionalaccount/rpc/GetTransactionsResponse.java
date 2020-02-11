package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions)
                .orElse(new TransactionsEntity())
                .toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.getLinks().getNext() != null);
    }

    @Override
    public String nextKey() {
        return transactions.getLinks().getNext().getHref();
    }
}
