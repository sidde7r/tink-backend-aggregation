package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionalAccountDto {
    private String accountNumber;
    private String uniqueIdentifier;
    private String description;
    private ExactCurrencyAmount exactBalance;

    public TransactionalAccountDto(
            String accountNumber,
            String uniqueIdentifier,
            String description,
            ExactCurrencyAmount exactBalance) {
        this.accountNumber = accountNumber;
        this.uniqueIdentifier = uniqueIdentifier;
        this.description = description;
        this.exactBalance = exactBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }
}
