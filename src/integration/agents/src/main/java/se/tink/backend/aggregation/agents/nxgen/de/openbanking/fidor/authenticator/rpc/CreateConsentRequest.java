package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateConsentRequest {
    private AccessEntity access;
    private String validUntil;
    private boolean recurringIndicator;
    private int frequencyPerDay;

    public CreateConsentRequest(
            String iban,
            String bban,
            String validUntil,
            boolean recurringIndicator,
            int frequencyPerDay) {
        this.access = new AccessEntity(iban, bban);
        this.validUntil = validUntil;
        this.recurringIndicator = recurringIndicator;
        this.frequencyPerDay = frequencyPerDay;
    }
}
