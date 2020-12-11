package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionDataEntity {
    @JsonProperty("Permissions")
    private Set<String> permissions;

    @JsonProperty("ExpirationDateTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expirationDateTime;

    protected AccountPermissionDataEntity() {}

    @JsonIgnore
    protected AccountPermissionDataEntity(Set<String> permissions, String expirationDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
    }

    public static AccountPermissionDataEntity create(Set<String> additionalPermissions) {
        Set<String> permissions = new HashSet<>(OpenIdAuthenticatorConstants.ACCOUNT_PERMISSIONS);

        if (Objects.nonNull(additionalPermissions)) {
            permissions.addAll(additionalPermissions);
        }
        return new AccountPermissionDataEntity(permissions, null);
    }
}
