package se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsEntity {

    @JsonProperty("_links")
    private FetcherLinksEntity links;

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return booked == null ? Collections.emptyList() : booked;
    }

    public List<TransactionEntity> getPending() {
        return pending == null ? Collections.emptyList() : pending;
    }
}
