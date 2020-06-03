package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Data
public class GetTransactionsResponse {
    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private List<TransactionEntity> transactions;

    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
