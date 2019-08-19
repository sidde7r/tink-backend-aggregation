package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanEntity {

    private final String iban;

    public IbanEntity(final String iban) {

        this.iban = iban;
    }
}
