package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountOwnerEntity {
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty private String role;

    public boolean isOwner() {
        return role.equalsIgnoreCase("owner");
    }

    @JsonIgnore
    public String getOwnerName() {
        return lastName;
    }
}
