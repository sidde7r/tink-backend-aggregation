package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PagerEntity {
    private int size;
    private int page;

    @JsonIgnore
    public PagerEntity(int size, int page) {
        this.size = size;
        this.page = page;
    }
}
