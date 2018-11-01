package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class CreditCardResponse {

    @JsonProperty("Movimientos")
    private List<CreditCardTransactionEntity> transactions;

    public List<AggregationTransaction> toTinkTransactions() {
        return transactions.stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
