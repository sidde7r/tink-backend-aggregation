package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetadataEntity {
    public int totalCount;
    public int resultCount;
    public int offset;

    @JsonIgnore
    public int getTotalCount() {
        return totalCount;
    }

    @JsonIgnore
    public int getResultCount() {
        return resultCount;
    }

    @JsonIgnore
    public int getOffset() {
        return offset;
    }
}
