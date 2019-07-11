package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountRepresentativeEntity {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("UserMask")
    private String userMask;
}
