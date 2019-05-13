package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessDetails {

    private String iban;

    public AccessDetails(String iban) {
        this.iban = iban;
    }
}
