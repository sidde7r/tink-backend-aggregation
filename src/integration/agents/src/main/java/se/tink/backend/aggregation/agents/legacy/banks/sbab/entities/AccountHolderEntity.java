package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Slf4j
public class AccountHolderEntity {
    @JsonProperty("fullName")
    private String name;

    @JsonProperty("orgPersNbr")
    private String socialSecurityNumber;

    private String partId;

    public HolderIdentity toHolderIdentity(HolderRole role) {
        HolderIdentity systemHolder = new HolderIdentity();
        systemHolder.setName(name);
        systemHolder.setRole(role);
        return systemHolder;
    }
}
