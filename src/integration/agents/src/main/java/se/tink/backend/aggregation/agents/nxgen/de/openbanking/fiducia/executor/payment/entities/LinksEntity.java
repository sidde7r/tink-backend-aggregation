package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkDetailsEntity startAuthorisationWithPsuAuthentication;
    private LinkDetailsEntity authoriseTransaction;

    public LinkDetailsEntity getAuthoriseTransaction() {
        return authoriseTransaction;
    }
}
