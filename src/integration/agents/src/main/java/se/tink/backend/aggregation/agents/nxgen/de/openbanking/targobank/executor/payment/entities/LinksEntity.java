package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private Href self;
    private Href status;
    private Href startAuthorisationWithPsuAuthentication;
}
