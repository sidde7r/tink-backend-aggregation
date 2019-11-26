package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionDto {
    private ExactCurrencyAmount amount;
    private String date;
    private String description;

    public TransactionDto(String amount, String currency, String date, String description) {
        this.amount = ExactCurrencyAmount.of(amount, currency);
        this.date = date;
        this.description = description;
    }

    public ExactCurrencyAmount getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
