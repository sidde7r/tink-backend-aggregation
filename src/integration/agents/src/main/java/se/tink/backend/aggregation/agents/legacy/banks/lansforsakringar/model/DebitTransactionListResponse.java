package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebitTransactionListResponse {
    private TransactionResponseEntity response;

    @JsonIgnore
    public boolean getHasMore() {
        return response.hasMore();
    }

    @JsonIgnore
    public List<TransactionEntity> getTransactions() {
        return response.getTransactions();
    }
}
