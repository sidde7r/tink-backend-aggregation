package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ConsentRequest {
    private ConsentAccess access;
    private boolean recurringIndicator;
    private String validUntil;
    private long frequencyPerDay;
    private boolean combinedServiceIndicator;
}
