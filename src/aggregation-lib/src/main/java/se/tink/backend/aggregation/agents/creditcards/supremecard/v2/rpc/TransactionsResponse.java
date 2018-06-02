package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.TransactionEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {
    private Boolean success;
    private List<TransactionEntity> data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<TransactionEntity> getData() {
        return data;
    }

    public void setData(List<TransactionEntity> data) {
        this.data = data;
    }
}
