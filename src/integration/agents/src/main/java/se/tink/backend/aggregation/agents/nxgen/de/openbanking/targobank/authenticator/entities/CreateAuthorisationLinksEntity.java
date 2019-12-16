package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateAuthorisationLinksEntity {
    private Href updatePsuAuthentication;
    private Href scaStatus;

    public Href getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }
}
