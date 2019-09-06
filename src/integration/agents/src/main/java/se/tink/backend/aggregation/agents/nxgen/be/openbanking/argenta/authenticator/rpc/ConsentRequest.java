package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.ConsentRequestAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    private String validUntil;
    private Boolean recurringIndicator;
    private ConsentRequestAccessEntity access;
    private int frequencyPerDay;

    @JsonCreator
    public ConsentRequest(
            @JsonProperty("validUntil") String validUntil,
            @JsonProperty("recurringIndicator") Boolean recurringIndicator,
            @JsonProperty("access") ConsentRequestAccessEntity access,
            @JsonProperty("frequencyPerDay") int frequencyPerDay) {
        this.validUntil = validUntil;
        this.recurringIndicator = recurringIndicator;
        this.access = access;
        this.frequencyPerDay = frequencyPerDay;
    }
}
