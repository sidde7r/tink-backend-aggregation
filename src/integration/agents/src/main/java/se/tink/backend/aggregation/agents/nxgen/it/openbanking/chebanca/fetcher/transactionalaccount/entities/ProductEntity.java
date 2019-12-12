package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
