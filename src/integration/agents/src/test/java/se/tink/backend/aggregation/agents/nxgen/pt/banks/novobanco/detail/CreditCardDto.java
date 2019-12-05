package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditCardDto {
    private String accountNumber;
    private String uniqueIdentifier;
    private String accountName;
    private ExactCurrencyAmount balance;
    private ExactCurrencyAmount availableCredit;

    public CreditCardDto(
            String accountNumber,
            String uniqueIdentifier,
            String accountName,
            ExactCurrencyAmount balance,
            ExactCurrencyAmount availableCredit) {
        this.accountNumber = accountNumber;
        this.uniqueIdentifier = uniqueIdentifier;
        this.accountName = accountName;
        this.balance = balance;
        this.availableCredit = availableCredit;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public String getAccountName() {
        return accountName;
    }

    public ExactCurrencyAmount getBalance() {
        return balance;
    }

    public ExactCurrencyAmount getAvailableCredit() {
        return availableCredit;
    }
}
