package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;

@JsonObject
public class AccountPermissionDataEntity {
    @JsonProperty("Permissions")
    private List<String> permissions;
    @JsonProperty("ExpirationDateTime")
    private String expirationDateTime;

    protected AccountPermissionDataEntity() {
    }

    @JsonIgnore
    protected AccountPermissionDataEntity(List<String> permissions, String expirationDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
    }

    public static AccountPermissionDataEntity create() {

        ZonedDateTime expireAt = ZonedDateTime.now(ZoneOffset.UTC).plus(Duration.ofHours(24));
        return new AccountPermissionDataEntity(
                OpenIdConstants.ACCOUNT_PERMISSIONS,
                expireAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}