package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {
    private String iban;
    private String currency;

    public String getCurrency() {
        return currency;
    }
}
