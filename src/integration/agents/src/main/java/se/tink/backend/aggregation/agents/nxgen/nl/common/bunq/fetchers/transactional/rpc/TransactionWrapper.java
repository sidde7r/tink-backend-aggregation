package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionWrapper {
    @JsonProperty("Payment")
    private TransactionEntity transaction;

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public Transaction toTinkTransaction() {
        return transaction.toTinkTransaction();
    }
}
