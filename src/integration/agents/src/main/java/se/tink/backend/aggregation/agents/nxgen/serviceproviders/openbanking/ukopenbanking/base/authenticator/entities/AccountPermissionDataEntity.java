package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionDataEntity {
    @JsonProperty("Permissions")
    private List<String> permissions;

    @JsonProperty("ExpirationDateTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expirationDateTime;

    protected AccountPermissionDataEntity() {}

    @JsonIgnore
    protected AccountPermissionDataEntity(List<String> permissions, String expirationDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
    }

    public static AccountPermissionDataEntity create(List<String> additionalPermissions) {
        List<String> permissions =
                new ArrayList<>(UkOpenBankingAisAuthenticatorConstants.ACCOUNT_PERMISSIONS);

        if (Objects.nonNull(additionalPermissions)) {
            permissions.addAll(additionalPermissions);
        }
        return new AccountPermissionDataEntity(permissions, null);
    }
}
