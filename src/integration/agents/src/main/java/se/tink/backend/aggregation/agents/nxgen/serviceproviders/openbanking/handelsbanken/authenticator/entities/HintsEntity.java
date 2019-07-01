package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class HintsEntity {

    @JsonProperty("allow")
    private List<String> allow;

    public List<String> getAllow() {
        return allow;
    }
}
