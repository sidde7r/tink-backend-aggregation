package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {
    private List<TransactionEntity> data;
    private boolean success;

    public List<TransactionEntity> getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setData(List<TransactionEntity> data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
