package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreateConsentResponse {

    private String consentId;
    private String consentStatus;
}
