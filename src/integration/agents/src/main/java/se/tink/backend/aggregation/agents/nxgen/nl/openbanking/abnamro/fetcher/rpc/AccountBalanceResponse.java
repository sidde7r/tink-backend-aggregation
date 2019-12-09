package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceResponse {

    private String accountNumber;
    private String balanceType;
    private String currency;
    private double amount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public Amount toAmount() {
        return new Amount(getCurrency(), getAmount());
    }
}
