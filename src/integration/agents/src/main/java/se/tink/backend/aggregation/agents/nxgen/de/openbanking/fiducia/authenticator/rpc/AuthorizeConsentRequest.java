package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.PsuData;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class AuthorizeConsentRequest {

    private PsuData psuData;
}
