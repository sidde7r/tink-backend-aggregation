package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private AccessEntity access;
    private String combinedServiceIndicator;
    private String frequencyPerDay;
    private String recurringIndicator;
    private String validUntil;

    public ConsentRequest(
            AccessEntity access,
            String combinedServiceIndicator,
            String frequencyPerDay,
            String recurringIndicator,
            String validUntil) {
        this.access = access;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
    }
}
