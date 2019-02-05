package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CatalogDataEntity {
    @JsonProperty("subtipo")
    private String subtype;

    @JsonProperty("producto")
    private String product;

    @JsonProperty("estandar")
    private String standard;

    public String getSubtype() {
        return subtype;
    }

    public String getProduct() {
        return product;
    }

    public String getStandard() {
        return standard;
    }
}
