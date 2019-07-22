package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private HrefEntity self;
    private HrefEntity status;
    private HrefEntity startAuthorisationWithPsuAuthentication;
}
