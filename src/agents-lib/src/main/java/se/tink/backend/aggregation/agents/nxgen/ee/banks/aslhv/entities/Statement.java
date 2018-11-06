package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Statement {

    @JsonProperty("transactions")
    private List<TransactionItem> transactions;

    @JsonIgnore
    public Optional<List<TransactionItem>> getTransactions() {
        return Optional.ofNullable(transactions);
    }
}
