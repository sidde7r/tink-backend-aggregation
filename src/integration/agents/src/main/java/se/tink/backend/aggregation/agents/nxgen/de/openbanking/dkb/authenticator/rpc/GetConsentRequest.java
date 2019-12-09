package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentRequest {
    private AccessEntity access;
    private Boolean combinedServiceIndicator;
    private Integer frequencyPerDay;
    private Boolean recurringIndicator;
    private String validUntil;

    public GetConsentRequest(
            AccessEntity access,
            Boolean combinedServiceIndicator,
            Integer frequencyPerDay,
            Boolean recurringIndicator,
            String validUntil) {
        this.access = access;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
    }
}
