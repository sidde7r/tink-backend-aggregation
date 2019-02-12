package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.TransactionListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    // @JsonProperty("_links") // Not yet implemented in API

    @JsonProperty("_embedded")
    private TransactionListEntity transactionList;

    public TransactionListEntity getTransactionList() {
        return transactionList;
    }
}
