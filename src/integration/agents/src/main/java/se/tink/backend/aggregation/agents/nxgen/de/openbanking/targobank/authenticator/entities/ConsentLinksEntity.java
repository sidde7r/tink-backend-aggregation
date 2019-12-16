package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {
    private Href self;
    private Href status;
    private Href startAuthorisationWithPsuAuthentication;

    public Href getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }
}
