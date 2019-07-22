package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date validUntil;

    private String frequencyPerDay;

    public ConsentRequest(
            AccessEntity access,
            boolean recurringIndicator,
            Date validUntil,
            String frequencyPerDay) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
    }

    public ConsentRequest() {
        AccessEntity access = new AccessEntity("allAccounts");
        this.access = access;
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        validUntil = now.getTime();
        frequencyPerDay = "4";
        recurringIndicator = true;
    }
}
