package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Hints {

    @JsonProperty("allow")
    private List<String> allow;

    public List<String> getAllow() {
        return allow;
    }
}
