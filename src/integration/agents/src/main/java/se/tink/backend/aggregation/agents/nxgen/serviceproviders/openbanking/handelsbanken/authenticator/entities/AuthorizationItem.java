package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationItem {

    private String name;

    private String href;

    private String type;

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }
}
