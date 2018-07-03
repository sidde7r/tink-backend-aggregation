package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.CollectionEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse {

    @JsonProperty("data")
    private ArrayList<TransactionEntity> transactions;

    @JsonProperty("collection")
    private CollectionEntity collectionEntity;

    public CollectionEntity getCollectionEntity() {
        return collectionEntity;
    }

    public ArrayList<TransactionEntity> getTransactions() {
        return transactions;
    }

    public List<Transaction> toTinkTransactions(){
        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
