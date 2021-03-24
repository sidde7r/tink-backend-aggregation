package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponseEntity {
    protected boolean moreExists;
    protected List<TransactionEntity> transactions;

    @JsonIgnore
    public boolean hasMore() {
        return moreExists;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }
}
