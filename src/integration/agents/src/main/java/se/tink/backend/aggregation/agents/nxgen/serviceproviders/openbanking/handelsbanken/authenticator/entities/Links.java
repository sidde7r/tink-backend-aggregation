package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Links {

    @JsonProperty("authorization")
    private List<AuthorizationItem> authorization;

    public List<AuthorizationItem> getAuthorization() {
        return authorization;
    }
}
