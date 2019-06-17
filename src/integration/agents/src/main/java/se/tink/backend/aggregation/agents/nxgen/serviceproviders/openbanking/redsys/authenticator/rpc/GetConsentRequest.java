package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentRequest {

    @JsonProperty private AccessEntity access;

    @JsonProperty private boolean recurringIndicator;

    @JsonProperty private String validUntil;

    @JsonProperty private int frequencyPerDay;

    @JsonProperty private boolean combinedServiceIndicator;

    public GetConsentRequest(
            AccessEntity access,
            boolean recurringIndicator,
            String validUntil,
            int frequencyPerDay,
            boolean combinedServiceIndicator) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
