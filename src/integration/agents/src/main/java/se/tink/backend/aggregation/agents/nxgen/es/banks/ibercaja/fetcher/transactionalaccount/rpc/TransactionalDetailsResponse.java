package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionalDetailsResponse implements PaginatorResponse {

    @JsonProperty("Paginacion")
    private boolean pagination;

    @JsonProperty("Movimientos")
    private List<TransactionDetailsEntity> transactions;

    @Override
    @JsonIgnore
    public Collection<? extends Transaction> getTinkTransactions() {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }
        return transactions.stream()
                .map(TransactionDetailsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
