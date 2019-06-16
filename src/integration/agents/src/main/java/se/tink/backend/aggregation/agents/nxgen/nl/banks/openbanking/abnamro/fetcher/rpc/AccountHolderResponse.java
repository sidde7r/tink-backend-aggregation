package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHolderResponse {

    private String accountNumber;
    private String currency;
    private String accountHolderName;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }
}
