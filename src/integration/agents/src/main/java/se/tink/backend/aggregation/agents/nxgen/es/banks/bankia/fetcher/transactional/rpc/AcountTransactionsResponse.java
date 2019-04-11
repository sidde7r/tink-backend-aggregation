package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AcountTransactionsResponse implements PaginatorResponse {
    @JsonProperty("indicadorPaginacion")
    private boolean paginacionIndicator;

    @JsonProperty("movimientos")
    private List<TransactionEntity> transactions;

    @JsonProperty("indicadorMasRegistros")
    private boolean moreRecordsIndicator;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // We cannot trust/use `moreRecordsIndicator` or `paginacionIndicator`.
        // `paginacionIndicator` is always True.
        // `moreRecordsIndicator` is always False.
        // If we paginate too far back we get a temporary error.
        // Let the pagination controller decide when to stop by returning Optional.empty().
        return Optional.empty();
    }
}
