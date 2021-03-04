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
    @JsonProperty("namn")
    private String name;

    @JsonProperty("personnummer")
    private String socialSecurityNumber;

    private String partId;

    @JsonProperty("kontohavartyp")
    private String role;

    public HolderIdentity toHolderIdentity() {
        HolderIdentity systemHolder = new HolderIdentity();
        systemHolder.setName(name);
        systemHolder.setRole(getHolderRole());
        return systemHolder;
    }

    private HolderRole getHolderRole() {
        if ("OWNER".equalsIgnoreCase(role)) {
            return HolderRole.HOLDER;
        }

        log.warn("Unknown holder role type {}", role);
        return HolderRole.OTHER;
    }
}
