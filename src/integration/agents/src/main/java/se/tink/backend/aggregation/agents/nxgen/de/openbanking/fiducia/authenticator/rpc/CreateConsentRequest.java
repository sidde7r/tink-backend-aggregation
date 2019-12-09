package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.Access;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentRequest {

    private Access access;
    private String frequencyPerDay;
    private String recurringIndicator;
    private String validUntil;
    private String combinedServiceIndicator;

    public CreateConsentRequest(
            Access access,
            String frequencyPerDay,
            String recurringIndicator,
            String validUntil,
            String combinedServiceIndicator) {
        this.access = access;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
