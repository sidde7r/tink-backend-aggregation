package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SearchCriteriaEntity {
    @JsonProperty("numeroMovimientosSolicitados")
    private int limit;

    @JsonIgnore
    public void setLimit(int limit) {
        this.limit = limit;
    }
}
