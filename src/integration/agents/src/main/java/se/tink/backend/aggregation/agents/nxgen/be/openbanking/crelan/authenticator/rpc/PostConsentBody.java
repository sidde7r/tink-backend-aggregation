package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PostConsentBody {
    private AccessEntity access;
    private Boolean combinedServiceIndicator;
    private int frequencyPerDay;
    private Boolean recurringIndicator;
    private String validUntil;

    public PostConsentBody(
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
