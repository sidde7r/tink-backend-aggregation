package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAgent {
    private IdentificationEntity identification;

    public CreditorAgent(IdentificationEntity identification) {
        this.identification = identification;
    }
}
