package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ResponseProduct {

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("name")
    private String name;

    @JsonProperty("version")
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
