package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private String frequencyPerDay;

    public CreateConsentRequest() {
        access = new AccessEntity();
        this.recurringIndicator = true;
        this.validUntil = FabricConstants.Consent.VALID_UNTIL;
        this.frequencyPerDay = FabricConstants.Consent.FREQUENCY_PER_DAY;
    }
}
