package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionPaginationEntity {
    @JsonProperty("continuation_key")
    private String continuationKey;

    public String getContinuationKey() {
        return continuationKey;
    }
}
