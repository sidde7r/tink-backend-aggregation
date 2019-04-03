package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.TransferEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransfersResponse implements PaginatorResponse {
    @JsonProperty("transfers")
    private List<TransferEntity> transfers;

    @JsonIgnore private boolean shouldFetchMore = true;

    @JsonIgnore
    public List<TransferEntity> getTransfers() {
        return Optional.ofNullable(transfers).orElse(Collections.emptyList());
    }

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        return getTransfers().stream()
                .map(TransferEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(shouldFetchMore && !getTransfers().isEmpty());
    }

    public TransfersResponse withShouldFetchMore(boolean shouldFetchMore) {
        this.shouldFetchMore = shouldFetchMore;
        return this;
    }
}
