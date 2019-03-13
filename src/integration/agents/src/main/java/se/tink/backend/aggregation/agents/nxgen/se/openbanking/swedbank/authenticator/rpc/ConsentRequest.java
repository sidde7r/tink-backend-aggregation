package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;
    private ConsentAccessEntity access;

    public ConsentRequest(
            Boolean recurringIndicator,
            String validUntil,
            Integer frequencyPerDay,
            Boolean combinedServiceIndicator,
            ConsentAccessEntity access) {
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.access = access;
    }
}
