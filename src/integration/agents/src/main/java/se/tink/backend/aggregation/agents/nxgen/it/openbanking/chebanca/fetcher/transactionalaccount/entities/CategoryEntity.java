package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoryEntity {
    @JsonProperty("name")
    private String name;

    @JsonProperty("products")
    private List<String> products;

    public CategoryEntity(String name, List<String> products) {
        this.name = name;
        this.products = products;
    }
}
