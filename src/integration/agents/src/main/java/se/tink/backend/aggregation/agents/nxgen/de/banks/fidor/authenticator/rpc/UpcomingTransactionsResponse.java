package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.CollectionEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionsResponse {

    @JsonProperty("data")
    private List<UpcomingTransactionEntity> transactions;
    @JsonProperty("collection")
    private CollectionEntity collectionEntity;

    public Collection<UpcomingTransaction> toUpcomingTransaction(){
        return transactions.stream()
                .map(UpcomingTransactionEntity::toUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
