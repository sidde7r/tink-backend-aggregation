package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaTransactionLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Data
public class ArkeaTransactionResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("transactions")
    private List<ArkeaTransactionEntity> transactionEntityList;

    @JsonProperty("_links")
    private ArkeaTransactionLinksEntity transactionLinksEntity;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactionEntityList).orElse(Collections.emptyList()).stream()
                .map(ArkeaTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactionLinksEntity.getNext() != null);
    }

    @Override
    public String nextKey() {
        return transactionLinksEntity.getNext().getHref();
    }
}
