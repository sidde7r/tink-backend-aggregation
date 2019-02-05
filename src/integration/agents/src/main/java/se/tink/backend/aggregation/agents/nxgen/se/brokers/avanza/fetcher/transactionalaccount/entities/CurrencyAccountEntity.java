package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrencyAccountEntity {
    private double balance;
    private String currency;

    public double getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
