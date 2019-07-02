package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HintsEntity {

    private List<String> allow;

    public List<String> getAllow() {
        return allow;
    }
}
