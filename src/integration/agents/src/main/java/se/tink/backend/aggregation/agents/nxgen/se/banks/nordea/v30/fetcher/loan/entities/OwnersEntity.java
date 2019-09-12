package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class OwnersEntity {
    @JsonProperty private String name;

    @Override
    public String toString() {
        return name;
    }

    @JsonIgnore
    public HolderName getName() {
        return new HolderName(name);
    }
}
