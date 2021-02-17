package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntity {

    private String iban;

    private AccountIdEntityOther other;

    public String getIban() {
        return iban;
    }

    public AccountIdEntityOther getOther() {
        return other;
    }
}
