package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    private ConsentAccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public ConsentRequest(
            ConsentAccessEntity access,
            Boolean recurringIndicator,
            String validUntil,
            Integer frequencyPerDay,
            Boolean combinedServiceIndicator) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
