package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class MovListItem {

    @JsonProperty("mov")
    private Mov mov;

    public Transaction getTinkTransactions() {
        return mov.toTinkTransaction();
    }

    public boolean isValid() {
        return mov.isValid();
    }
}
