package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MenuItemLinkEntity extends LinkEntity {
    public enum Authorization {
        AUTHORIZED, REQUIRES_AUTH_METHOD_CHANGE, UNAUTHORIZED
    }

    private String name;
    private Authorization authorization;

    public String getName() {
        return name;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    @JsonIgnore
    public boolean isAuthorized() {
        return Authorization.AUTHORIZED == authorization;
    }

    @JsonIgnore
    public boolean isRequiresChangeAuthMethod() {
        return Authorization.REQUIRES_AUTH_METHOD_CHANGE == authorization;
    }

    @JsonIgnore
    public boolean isUnauthroized() {
        return Authorization.UNAUTHORIZED == authorization;
    }
}
