package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class AuthorizationItem {

    @JsonProperty("name")
    private String name;

    @JsonProperty("href")
    private String href;

    @JsonProperty("type")
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
