package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MenuItemLinkEntity extends LinkEntity {
    private String name;
    private String authorization;

    public String getName() {
        return name;
    }

    public String getAuthorization() {
        return authorization;
    }

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
        return SwedbankBaseConstants.Authorization.REQUIRES_AUTH_METHOD_CHANGE == getAuthorizationValue();
    }

    @JsonIgnore
    public boolean isUnauthroized() {
        return SwedbankBaseConstants.Authorization.UNAUTHORIZED == getAuthorizationValue();
    }
}
