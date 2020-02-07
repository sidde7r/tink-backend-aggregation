package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.entity.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private String validUntil;
    private ConsentRequestAccessEntity access;
    private int frequencyPerDay;
    private Boolean recurringIndicator;
    private Boolean combinedServiceIndicator;

    @JsonCreator
    public ConsentRequest(
            @JsonProperty("validUntil") String validUntil,
            @JsonProperty("access") ConsentRequestAccessEntity access,
            @JsonProperty("frequencyPerDay") int frequencyPerDay,
            @JsonProperty("recurringIndicator") Boolean recurringIndicator,
            @JsonProperty("combinedServiceIndicator") Boolean combinedServiceIndicator) {
        this.validUntil = validUntil;
        this.access = access;
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
