package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MenuItemLinkEntity extends LinkEntity {
    private String name;
    private String authorization;

    @JsonIgnore
    public SwedbankBaseConstants.Authorization getAuthorizationValue() {
        return SwedbankBaseConstants.Authorization.fromAuthorizationString(authorization);
    }

    @JsonIgnore
    public boolean isAuthorized() {
        return SwedbankBaseConstants.Authorization.AUTHORIZED == getAuthorizationValue();
    }

    @JsonIgnore
    public boolean isRequiresChangeAuthMethod() {
        return SwedbankBaseConstants.Authorization.REQUIRES_AUTH_METHOD_CHANGE
                == getAuthorizationValue();
    }

    @JsonIgnore
    public boolean isUnauthroized() {
        return SwedbankBaseConstants.Authorization.UNAUTHORIZED == getAuthorizationValue();
    }
}
