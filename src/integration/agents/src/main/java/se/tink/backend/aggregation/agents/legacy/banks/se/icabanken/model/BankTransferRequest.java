package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankTransferRequest extends TransferRequest {
    @JsonProperty("IsStandingTransaction")
    private boolean standingTransaction = false;

    public Boolean getStandingTransaction() {
        return standingTransaction;
    }

    public void setStandingTransaction(boolean standingTransaction) {
        this.standingTransaction = standingTransaction;
    }
}
