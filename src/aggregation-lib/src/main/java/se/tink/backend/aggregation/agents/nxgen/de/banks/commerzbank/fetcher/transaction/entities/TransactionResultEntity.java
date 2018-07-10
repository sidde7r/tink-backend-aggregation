package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResultEntity {

    @JsonProperty("items")
    private List<TransactionEntity> items;

    public List<TransactionEntity> getItems() {
        return items;
    }
    // `metaData` is null - cannot define it!
}
