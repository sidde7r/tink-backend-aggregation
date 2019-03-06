package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FakeAccount {

    //TODO: Align this with how the account looks in the Demo Fake Bank
    @JsonProperty
    private String accountType;
    @JsonProperty
    private double balance;
    @JsonProperty
    private String accountNumber;

    public FakeAccount(String accountType, double balance, String accountNumber) {
        this.accountType = accountType;
        this.balance = balance;
        this.accountNumber = accountNumber;
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
}
