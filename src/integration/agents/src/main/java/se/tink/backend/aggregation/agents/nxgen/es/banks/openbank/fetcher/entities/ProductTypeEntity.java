package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductTypeEntity {
    @JsonProperty("empresa")
    private String company;

    @JsonProperty("tipodeproducto")
    private String type;

    public String getCompany() {
        return company;
    }

    public String getType() {
        return type;
    }
}
