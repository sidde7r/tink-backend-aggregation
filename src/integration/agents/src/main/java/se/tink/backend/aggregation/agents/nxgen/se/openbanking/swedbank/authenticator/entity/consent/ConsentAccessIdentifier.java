package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.entity.consent;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAccessIdentifier {
    private String iban;

    public ConsentAccessIdentifier(String iban) {
        this.iban = iban;
    }
}
