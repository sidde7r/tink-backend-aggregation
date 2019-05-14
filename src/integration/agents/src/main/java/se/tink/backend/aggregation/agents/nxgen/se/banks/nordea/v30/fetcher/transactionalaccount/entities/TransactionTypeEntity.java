package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionTypeEntity {
    @JsonProperty("transaction_code")
    private String transactionCode;

    @JsonProperty("transaction_code_text")
    private String transactionCodeText;

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getTransactionCodeText() {
        return transactionCodeText;
    }
}
