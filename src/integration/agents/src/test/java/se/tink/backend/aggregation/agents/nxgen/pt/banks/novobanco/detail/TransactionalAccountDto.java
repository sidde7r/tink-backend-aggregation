package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountDto {
    private String accountNumber;
    private String uniqueIdentifier;
    private ExactCurrencyAmount exactBalance;

    public TransactionalAccountDto(
            String accountNumber, String uniqueIdentifier, ExactCurrencyAmount exactBalance) {
        this.accountNumber = accountNumber;
        this.uniqueIdentifier = uniqueIdentifier;
        this.exactBalance = exactBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }
}
