package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterEntity {
    @JsonProperty("valido")
    private String valid;

    @JsonProperty("bloque")
    private Object block;

    public String getValid() {
        return valid;
    }

    public Object getBlock() {
        return block;
    }
}
