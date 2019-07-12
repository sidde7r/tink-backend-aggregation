package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntity {
    private String iban;
    private IdDetailsEntity other;

    public String getIban() {
        return iban;
    }
}
