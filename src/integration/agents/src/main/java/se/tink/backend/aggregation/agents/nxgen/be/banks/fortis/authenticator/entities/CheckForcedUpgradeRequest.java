package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CheckForcedUpgradeRequest {
    private final String distributorId;

    public CheckForcedUpgradeRequest(String distributorId) {
        this.distributorId = distributorId;
    }
}
