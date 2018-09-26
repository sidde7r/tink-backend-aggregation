package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    @JsonProperty("ProductName")
    private String productName;
    @JsonProperty("ProductImage")
    private String productImage;

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }
}
