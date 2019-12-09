package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentBaseRequest {
    @JsonProperty private final AccessEntity access;
    @JsonProperty private final boolean recurringIndicator;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final Date validUntil;

    @JsonProperty private final int frequencyPerDay;
    @JsonProperty private final boolean combinedServiceIndicator;

    public ConsentBaseRequest(String iban, String currency) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.access = new AccessEntity(iban, currency);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }
}
