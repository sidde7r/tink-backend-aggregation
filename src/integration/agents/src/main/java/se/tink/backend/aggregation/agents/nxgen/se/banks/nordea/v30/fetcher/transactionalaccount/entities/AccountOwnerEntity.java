package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class AccountOwnerEntity {
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty private String role;

    public boolean isOwner() {
        return role.equalsIgnoreCase("owner");
    }

    @JsonIgnore
    public HolderName getHolderName() {
        return new HolderName(lastName);
    }

    @JsonIgnore
    public String getOwnerName() {
        return lastName;
    }
}
