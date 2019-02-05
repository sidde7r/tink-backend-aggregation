package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String name;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormatterNumber;

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormatterNumber;
    }
}
