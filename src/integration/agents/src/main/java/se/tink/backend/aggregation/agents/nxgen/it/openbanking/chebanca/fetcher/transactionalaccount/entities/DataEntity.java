package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {
    private String customerid;

    @JsonIgnore
    public String getCustomerid() {
        return customerid;
    }
}
