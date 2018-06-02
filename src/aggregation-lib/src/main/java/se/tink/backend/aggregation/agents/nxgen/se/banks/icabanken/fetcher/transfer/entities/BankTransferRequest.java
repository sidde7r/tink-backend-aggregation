package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
