package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.PsuData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeConsentRequest {
    private PsuData psuData;

    public AuthorizeConsentRequest(PsuData psuData) {
        this.psuData = psuData;
    }
}
