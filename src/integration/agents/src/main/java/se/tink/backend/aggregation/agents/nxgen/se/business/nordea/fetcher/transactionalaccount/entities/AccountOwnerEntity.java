package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
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

    @JsonIgnore
    public boolean hasOwnerName() {
        return !Strings.isNullOrEmpty(lastName);
    }
}
