package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditConsentRequest implements ConsentRequest {

    private ConsentAccessEntity access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private Boolean combinedServiceIndicator;

    public UnicreditConsentRequest(
            ConsentAccessEntity access,
            Boolean recurringIndicator,
            String validUntil,
            Integer frequencyPerDay,
            Boolean combinedServiceIndicator) {
        this.access = access;
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
