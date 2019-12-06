package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public ConsentRequest(
            AccessEntity access,
            boolean recurringIndicator,
            String validUntil,
            int frequencyPerDay,
            boolean combinedServiceIndicator) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
