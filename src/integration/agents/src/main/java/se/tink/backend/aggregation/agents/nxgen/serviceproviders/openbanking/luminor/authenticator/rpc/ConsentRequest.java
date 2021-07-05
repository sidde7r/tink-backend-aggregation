package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    @JsonProperty("access")
    private ConsentRequestAccessEntity access;

    private int frequencyPerDay;
    private boolean recurringIndicator;
    // (YYYY-MM-DD)
    private String validUntil;

    // Currently not supported
    private boolean combinedServiceIndicator;

    @JsonCreator
    public ConsentRequest(
            ConsentRequestAccessEntity access,
            int frequencyPerDay,
            boolean recurringIndicator,
            String validUntil,
            boolean combinedServiceIndicator) {
        this.access = access;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
