package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private ConsentAccess access;
    private boolean recurringIndicator;
    private String validUntil;
    private long frequencyPerDay;
    private boolean combinedServiceIndicator;

    public ConsentRequest(
            ConsentAccess access,
            boolean recurringIndicator,
            String validUntil,
            long frequencyPerDay,
            boolean combinedServiceIndicator) {

        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
