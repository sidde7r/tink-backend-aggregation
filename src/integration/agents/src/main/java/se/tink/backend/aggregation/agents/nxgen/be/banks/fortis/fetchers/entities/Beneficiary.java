package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Beneficiary {
    private String name;
    private String accountNumber;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
