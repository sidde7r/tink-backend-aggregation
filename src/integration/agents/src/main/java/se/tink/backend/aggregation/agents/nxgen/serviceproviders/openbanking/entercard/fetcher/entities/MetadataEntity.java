package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetadataEntity {
    private long offset;

    @JsonProperty("result_count")
    private long resultCount;

    @JsonProperty("total_count")
    private long totalCount;

    public long getTotalCount() {
        return totalCount;
    }

    public long getResultCount() {
        return resultCount;
    }

    public long getOffset() {
        return offset;
    }
}
