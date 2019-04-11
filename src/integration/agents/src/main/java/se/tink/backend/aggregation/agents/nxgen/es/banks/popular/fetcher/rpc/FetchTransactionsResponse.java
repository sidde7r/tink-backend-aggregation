package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.TransactionsWrapperEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse extends BancoPopularResponse implements PaginatorResponse {
    private TransactionsWrapperEntity customBtd6ECOAS211F;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        if (customBtd6ECOAS211F != null && customBtd6ECOAS211F.getTransactionList() != null) {
            return customBtd6ECOAS211F.getTransactionList().stream()
                    .map(TransactionEntity::toTinkTransaction)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // Possible fields for deciding if we can fetch more doesn't actually indicate this.
        // hasMore is always "N" and nextPage is always an empty string. Leaving it up to the
        // paginator to
        // stop fetching once we've gotten two empty pages in a row.
        return Optional.empty();
    }

    public TransactionsWrapperEntity getCustomBtd6ECOAS211F() {
        return customBtd6ECOAS211F;
    }
}
