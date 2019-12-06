package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PostConsentBodyRequest {

    private AccessEntity access;
    private Boolean combinedServiceIndicator;
    private int frequencyPerDay;
    private Boolean recurringIndicator;
    private String validUntil;

    public PostConsentBodyRequest(
            AccessEntity access,
            Boolean combinedServiceIndicator,
            int frequencyPerDay,
            Boolean recurringIndicator,
            String validUntil) {
        this.access = access;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
    }
}
