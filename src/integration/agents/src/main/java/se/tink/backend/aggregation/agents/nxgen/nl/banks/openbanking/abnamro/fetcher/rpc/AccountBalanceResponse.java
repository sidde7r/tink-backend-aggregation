package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountBalanceResponse {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("balanceType")
    private String balanceType;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private double amount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }

    @JsonIgnore
    public Amount toAmount() {
        return new Amount(getCurrency(), getAmount());
    }
}
