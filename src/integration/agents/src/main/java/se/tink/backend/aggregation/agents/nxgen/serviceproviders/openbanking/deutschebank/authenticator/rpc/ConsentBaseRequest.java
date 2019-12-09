package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseRequest {

    private final AccessEntity access;
    private final boolean recurringIndicator;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date validUntil;

    private final int frequencyPerDay;
    private final boolean combinedServiceIndicator;

    public ConsentBaseRequest(String iban) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.access = new AccessEntity(iban);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }
}
