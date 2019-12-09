package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity.ConsentAccessEntity;
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
