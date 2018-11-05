package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractSubtypeEntity {
    @JsonProperty("tipodeproducto")
    private ProductTypeEntity productType;

    @JsonProperty("subtipodeproducto")
    private String productSubtype;

    public ProductTypeEntity getProductType() {
        return productType;
    }

    public String getProductSubtype() {
        return productSubtype;
    }
}
