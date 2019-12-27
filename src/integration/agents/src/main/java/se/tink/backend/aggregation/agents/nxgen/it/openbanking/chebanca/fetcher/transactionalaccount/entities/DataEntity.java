package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {
    @JsonProperty("customerid")
    private String customerid;

    public String getCustomerid() {
        return customerid;
    }
}
