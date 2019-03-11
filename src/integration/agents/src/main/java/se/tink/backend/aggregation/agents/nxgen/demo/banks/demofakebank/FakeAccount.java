package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FakeAccount {

    @JsonProperty("accountType")
    private AccountType accountType;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("balance")
    private double balance;
    @JsonProperty("currency")
    private String currency;

    public FakeAccount() {
    }

    @JsonIgnore
    public FakeAccount(AccountType accountType, String accountNumber, double balance, String currency) {
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
    }

    @JsonIgnore
    public CheckingAccount toTinkCheckingAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(getAccountNumber())
                .setAccountNumber(getAccountNumber())
                .setBalance(Amount.inSEK(getBalance())) // TODO: What is the currency?
                .addAccountIdentifier(
                        new SwedishIdentifier(
                                getAccountNumber())) // TODO: What should the identifier be? clearing etc?
                .build();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
