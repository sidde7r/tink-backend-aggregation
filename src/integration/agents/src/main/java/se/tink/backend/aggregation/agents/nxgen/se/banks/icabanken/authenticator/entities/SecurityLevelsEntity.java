package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityLevelsEntity {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Permissions")
    private PermissionsEntity permissions;

    public String getId() {
        return id;
    }

    public PermissionsEntity getPermissions() {
        return permissions;
    }
}
