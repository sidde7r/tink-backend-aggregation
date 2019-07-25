package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseProductEntity {

    private String scope;

    private String name;

    private String version;

    public String getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
