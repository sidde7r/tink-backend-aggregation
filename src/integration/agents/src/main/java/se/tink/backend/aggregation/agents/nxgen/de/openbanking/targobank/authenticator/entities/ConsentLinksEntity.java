package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {
    private HrefEntity self;
    private HrefEntity status;
    private HrefEntity startAuthorisationWithPsuAuthentication;

    public HrefEntity getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }
}
