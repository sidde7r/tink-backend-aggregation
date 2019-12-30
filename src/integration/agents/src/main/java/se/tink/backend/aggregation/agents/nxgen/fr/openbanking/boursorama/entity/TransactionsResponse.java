package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }
}
