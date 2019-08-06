package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    private AccessEntity access;

    private boolean combinedServiceIndicator;

    private String validUntil;

    private boolean recurringIndicator;

    private int frequencyPerDay;

    public AccessEntity getAccess() {
        return access;
    }

    public boolean getCombinedServiceIndicator() {
        return combinedServiceIndicator;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public boolean getRecurringIndicator() {
        return recurringIndicator;
    }

    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public ConsentRequest(
            AccessEntity access,
            boolean combinedServiceIndicator,
            String validUntil,
            boolean recurringIndicator,
            int frequencyPerDay) {
        this.access = access;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
    }
}
