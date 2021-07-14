package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    private AccessEntity access;
    private boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public ConsentRequest(AccessEntity accessEntity, LocalDate date) {
        this.access = accessEntity;
        this.recurringIndicator = true;
        this.validUntil = date.plusDays(Values.HISTORY_MAX_DAYS);
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }
}
