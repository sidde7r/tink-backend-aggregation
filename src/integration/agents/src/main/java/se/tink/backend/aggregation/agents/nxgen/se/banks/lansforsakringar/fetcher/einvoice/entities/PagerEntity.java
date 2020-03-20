package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PagerEntity {
    private int startIndex;
    private int size;

    @JsonIgnore
    public PagerEntity(int startIndex, int size) {
        this.startIndex = startIndex;
        this.size = size;
    }
}
