package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String iban;
    private String bban;

    public AccountEntity(String iban, String bban) {
        this.iban = iban;
        this.bban = bban;
    }
}
