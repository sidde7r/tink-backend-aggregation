package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OwnersEntity {
    @JsonProperty private String name;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
