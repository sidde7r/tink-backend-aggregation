package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntity {
    private String iban;
    private IdDetailsEntity other;

    public String getIban() {
        return iban;
    }
}
