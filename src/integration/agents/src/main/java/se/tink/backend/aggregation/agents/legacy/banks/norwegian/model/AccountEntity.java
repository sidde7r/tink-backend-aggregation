package se.tink.backend.aggregation.agents.banks.norwegian.model;

import com.google.common.base.Strings;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;

public class AccountEntity {

    private static Pattern MASKED_CREDIT_CARD_NUMBER_PATTERN =
            Pattern.compile("[0-9]{6}[*]{6}[0-9]{4}");
    private static Pattern ACCOUNT_ID_PATTERN = Pattern.compile("[0-9]{8,}");

    private Double availableCredit;
    private Double balance;
    private String accountNumber;
    private String cardNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Double getAvailableCredit() {
        return availableCredit;
    }

    public void setAvailableCredit(Double available) {
        this.availableCredit = available;
    }

    public Account toTinkAccount() {
        Account tinkAccount = new Account();
        tinkAccount.setBalance(balance);
        tinkAccount.setBankId("NORWEGIAN_CARD");

        // Only support credit card right now
        tinkAccount.setType(AccountTypes.CREDIT_CARD);

        // Use card number if available
        if (Strings.isNullOrEmpty(cardNumber)) {
            tinkAccount.setName("Norwegiankortet");
            tinkAccount.setAccountNumber(accountNumber);
        } else {
            tinkAccount.setName(cardNumber);
            tinkAccount.setAccountNumber(cardNumber);
        }

        if (null != availableCredit) {
            tinkAccount.setAvailableCredit(availableCredit.doubleValue());
        }

        return tinkAccount;
    }

    public boolean hasValidBankId() {
        return MASKED_CREDIT_CARD_NUMBER_PATTERN.matcher(accountNumber).matches()
                || ACCOUNT_ID_PATTERN.matcher(accountNumber).matches();
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
