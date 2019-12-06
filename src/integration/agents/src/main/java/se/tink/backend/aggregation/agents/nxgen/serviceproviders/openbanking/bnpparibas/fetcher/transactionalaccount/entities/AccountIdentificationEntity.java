package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdentificationEntity {

    private String iban;

    private String currency;

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }
}
