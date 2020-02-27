package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseEntity {
    private boolean moreExists;
    private List<TransactionsEntity> transactions;

    @JsonIgnore
    public boolean hasMore() {
        return moreExists;
    }

    @JsonIgnore
    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setTransactions(List<TransactionsEntity> transactions) {
        this.transactions = transactions;
    }
}
