package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.Access;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class CreateConsentRequest {

    private Access access;
    private String frequencyPerDay;
    private String recurringIndicator;
    private String validUntil;
    private String combinedServiceIndicator;
}
