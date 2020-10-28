package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AgreementEntity {
    private boolean isActive;
    private boolean isUnlocked;
}
