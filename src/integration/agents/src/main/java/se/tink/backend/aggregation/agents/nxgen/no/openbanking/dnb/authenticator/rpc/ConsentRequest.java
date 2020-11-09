package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ConsentRequestValues;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    private AccessEntity access;
    private String validUntil;
    private int frequencyPerDay;
    private boolean recurringIndicator;
    private boolean combinedServiceIndicator;

    public ConsentRequest() {
        this.access = new AccessEntity();
        this.validUntil =
                LocalDate.now().plusDays(ConsentRequestValues.CONSENT_DAYS_VALID).toString();
        this.frequencyPerDay = ConsentRequestValues.FREQUENCY_PER_DAY;
        this.recurringIndicator = ConsentRequestValues.RECURRING;
        this.combinedServiceIndicator = ConsentRequestValues.COMBINED_SERVICE;
    }
}
