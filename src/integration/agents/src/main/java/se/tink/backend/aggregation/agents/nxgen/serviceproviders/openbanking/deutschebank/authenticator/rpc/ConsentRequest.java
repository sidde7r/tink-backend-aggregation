package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@JsonObject
public class ConsentRequest {

    private final AccessEntity access;
    private final boolean recurringIndicator;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date validUntil;

    private final int frequencyPerDay;
    private final boolean combinedServiceIndicator;

    public ConsentRequest(AccessEntity accessEntity, LocalDateTimeSource localDateTimeSource) {
        LocalDateTime validUntilLocalDate = localDateTimeSource.now().plusDays(90);
        this.access = accessEntity;
        this.recurringIndicator = true;
        this.validUntil = Date.from(validUntilLocalDate.atZone(ZoneId.systemDefault()).toInstant());
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }
}
