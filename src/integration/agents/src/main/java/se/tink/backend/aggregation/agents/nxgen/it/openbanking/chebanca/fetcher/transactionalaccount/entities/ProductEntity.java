package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductEntity {
    private String code;
    private String description;

    @JsonIgnore
    public String getDescription() {
        return description;
    }
}
