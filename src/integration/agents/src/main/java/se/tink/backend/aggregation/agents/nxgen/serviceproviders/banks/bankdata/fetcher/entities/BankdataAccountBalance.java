package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataAccountBalance {
    private double balance;
    private double available;

    public double getBalance() {
        return balance;
    }

    public double getAvailable() {
        return available;
    }
}
