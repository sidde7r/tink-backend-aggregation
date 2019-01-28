package se.tink.backend.aggregation.agents.banks.norwegian.model;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

public class AccountEntity {

    private static Pattern ALLOWED_BANK_ID_PATTERN = Pattern.compile("[0-9]{6}[*]{6}[0-9]{4}");

    private Double balance;
    private String accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Account toTinkAccount() {
        Account tinkAccount = new Account();
        tinkAccount.setBalance(balance);
        tinkAccount.setAccountNumber(accountNumber);
        tinkAccount.setBankId("NORWEGIAN_CARD");

        // Only support credit card right now
        tinkAccount.setType(AccountTypes.CREDIT_CARD);
        tinkAccount.setName("Norwegiankortet");

        return tinkAccount;
    }

    public boolean hasValidBankId() {
        return ALLOWED_BANK_ID_PATTERN.matcher(accountNumber).matches();
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
