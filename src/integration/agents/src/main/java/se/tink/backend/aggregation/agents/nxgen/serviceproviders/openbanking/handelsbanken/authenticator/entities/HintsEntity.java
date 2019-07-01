package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class HintsEntity {

    private List<String> allow;

    public List<String> getAllow() {
        return allow;
    }
}
