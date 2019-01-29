package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class AccountOwnerEntity {
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty
    private String role;

    public boolean isOwner(){
        return role.equalsIgnoreCase("owner");
    }

    public HolderName getHolderName() {
        return new HolderName(lastName);
    }


}
