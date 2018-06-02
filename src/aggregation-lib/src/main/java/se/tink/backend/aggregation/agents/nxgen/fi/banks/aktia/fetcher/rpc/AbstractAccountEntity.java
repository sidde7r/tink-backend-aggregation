package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AbstractAccountEntity {
    protected String id;
    protected String name;
    protected String iban;
    protected double balance;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public double getBalance() {
        return balance;
    }
}
