package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceApproveResponse extends AbstractChallengeResponse {
    // We don't really need this entity, but we can use it for debugging if we'd like
    @JsonProperty("Transaction")
    private String transaction;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(JsonNode transaction) {
        this.transaction = transaction != null ? transaction.toString() : null;
    }
}
