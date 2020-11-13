package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ConsentRequest {

    private AccessEntity access;
    private Boolean combinedServiceIndicator;
    private int frequencyPerDay;
    private Boolean recurringIndicator;
    private String validUntil;
}
