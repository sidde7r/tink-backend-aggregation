package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHolderEntity {
    @JsonProperty("identitet")
    private String identity;

    @JsonProperty("namn")
    private String name;

    @JsonProperty("roll")
    private String role;

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
