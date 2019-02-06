package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity.DataItemEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {
    private PaginationEntity pagination;
    private List<DataItemEntity> data;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return data.stream()
                .filter(x -> x.isValid())
                .map(DataItemEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // When there are no more transactions to fetch, the TransactionsResponse will be null.
        // This is handled in the fetcher
        return Optional.of(true);
    }
}
