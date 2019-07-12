package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    private String iban;
    private String currency;
    private String href;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public String getHref() {
        return href;
    }
}
