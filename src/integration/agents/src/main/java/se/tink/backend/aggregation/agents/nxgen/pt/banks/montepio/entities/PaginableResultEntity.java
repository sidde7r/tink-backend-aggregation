package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class PaginableResultEntity {

    @JsonProperty("HasMorePages")
    private Boolean hasMorePages;

    public boolean hasMorePages() {
        return Boolean.TRUE.equals(hasMorePages);
    }
}
