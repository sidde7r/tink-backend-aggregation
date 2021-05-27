package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ConsentRequest {
    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;
}
