package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.ToString;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@ToString
public class ConsentRequest {

    private AccessEntity access;
    private boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    private int frequencyPerDay;
    private boolean combinedServiceIndicator;

    public ConsentRequest(AccessEntity accessEntity) {
        this.access = accessEntity;
        this.recurringIndicator = true;
        this.validUntil = LocalDate.now().plusDays(89);
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }
}
