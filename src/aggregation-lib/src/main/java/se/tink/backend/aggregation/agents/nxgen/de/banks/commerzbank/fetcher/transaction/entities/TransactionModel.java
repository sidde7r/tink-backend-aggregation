package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionModel {
    // `error` is null - cannot define it!
    @JsonProperty("result")
    private TransactionResultEntity result;

    public TransactionResultEntity getResult() {
        return result;
    }
}
