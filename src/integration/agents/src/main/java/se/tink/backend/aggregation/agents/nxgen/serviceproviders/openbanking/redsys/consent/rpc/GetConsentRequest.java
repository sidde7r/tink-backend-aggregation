package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetConsentRequest {

    @JsonProperty private AccessEntity access;

    @JsonProperty private boolean recurringIndicator;

    @JsonProperty private String validUntil;

    @JsonProperty private int frequencyPerDay;

    @JsonProperty private boolean combinedServiceIndicator;

    @JsonIgnore
    public GetConsentRequest(
            AccessEntity access,
            boolean recurringIndicator,
            LocalDate validUntil,
            int frequencyPerDay,
            boolean combinedServiceIndicator) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil.format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
