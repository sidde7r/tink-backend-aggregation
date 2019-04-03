package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {

    public ConsentAccessEntity access;
    public Boolean recurringIndicator;
    public String validUntil;
    public Integer frequencyPerDay;

    public ConsentRequest(
            ConsentAccessEntity access,
            Boolean recurringIndicator,
            String validUntil,
            Integer frequencyPerDay) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
    }
}
