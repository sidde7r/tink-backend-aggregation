package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest<T> {
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;
    private T access;

    public ConsentRequest(
            Boolean recurringIndicator,
            String validUntil,
            Integer frequencyPerDay,
            Boolean combinedServiceIndicator,
            T access) {
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.access = access;
    }
}
