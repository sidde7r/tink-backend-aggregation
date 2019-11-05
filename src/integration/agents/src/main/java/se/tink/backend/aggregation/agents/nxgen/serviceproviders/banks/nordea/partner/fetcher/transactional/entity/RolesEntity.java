package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RolesEntity {
    private String name;
    private String role;

    public boolean isOwner() {
        return role != null && role.toUpperCase().equals("OWNER");
    }

    public String getName() {
        return name;
    }
}
