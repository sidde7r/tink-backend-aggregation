package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MortgageDeposit {

    // The deposit amount in SEK (required).
    @JsonProperty("belopp")
    private Integer amount;

    // The account number for the bank account to which the deposit should be payed (not required).
    @JsonProperty("konto")
    private String accountNumber;

    // The desired date for the payout of the deposit (not required).
    @JsonProperty("utbetalningsdag")
    private String desiredPayoutDay;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDesiredPayoutDay() {
        return desiredPayoutDay;
    }

    public void setDesiredPayoutDay(String desiredPayoutDay) {
        this.desiredPayoutDay = desiredPayoutDay;
    }
}
