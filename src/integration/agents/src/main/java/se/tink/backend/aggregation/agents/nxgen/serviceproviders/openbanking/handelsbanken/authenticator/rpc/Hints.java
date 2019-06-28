package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class Hints {

    @JsonProperty("allow")
    private List<String> allow;

    public List<String> getAllow() {
        return allow;
    }
}
