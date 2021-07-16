package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterEntity {
    private List<IncludesEntity> includes;

    @JsonIgnore
    public FilterEntity(List<IncludesEntity> includes) {
        this.includes = includes;
    }
}
