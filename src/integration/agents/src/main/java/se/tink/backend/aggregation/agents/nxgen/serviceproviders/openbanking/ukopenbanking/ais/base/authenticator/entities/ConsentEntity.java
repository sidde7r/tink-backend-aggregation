package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentEntity {
    @JsonProperty("Permissions")
    private Set<String> permissions;

    @JsonProperty("ExpirationDateTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expirationDateTime;

    protected ConsentEntity() {}

    @JsonIgnore
    protected ConsentEntity(Set<String> permissions, String expirationDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
    }

    @JsonIgnore
    protected ConsentEntity(Set<String> permissions) {
        this.permissions = permissions;
    }

    public static ConsentEntity of(Set<String> permissions) {
        return new ConsentEntity(permissions);
    }

    public static ConsentEntity of(Set<String> permissions, String expirationDateTime) {
        return new ConsentEntity(permissions, expirationDateTime);
    }
}
