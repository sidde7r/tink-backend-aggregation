package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementEntity {
    private boolean isActive;
    private boolean isUnlocked;

    public boolean isActive() {
        return isActive;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
