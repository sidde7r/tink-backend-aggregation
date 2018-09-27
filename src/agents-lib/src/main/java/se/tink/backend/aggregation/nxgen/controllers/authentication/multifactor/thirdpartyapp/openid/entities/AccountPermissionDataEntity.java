package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.JwtUtils;

@JsonObject
public class AccountPermissionDataEntity {
    @JsonProperty("Permissions")
    private List<String> permissions;
    @JsonProperty("ExpirationDateTime")
    private String expirationDateTime;

    protected AccountPermissionDataEntity() { }

    @JsonIgnore
    protected AccountPermissionDataEntity(List<String> permissions, String expirationDateTime) {
        this.permissions = permissions;
        this.expirationDateTime = expirationDateTime;
    }

    public static AccountPermissionDataEntity create() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"); //TODO: Create util for this.

        String expireAt = format.format(JwtUtils.addHours(new Date(), 24));

        return new AccountPermissionDataEntity(
                OpenIdConstants.ACCOUNT_PERMISSIONS,
                expireAt);
    }
}
    //Fri Sep 28 11:55:39 UTC 2018