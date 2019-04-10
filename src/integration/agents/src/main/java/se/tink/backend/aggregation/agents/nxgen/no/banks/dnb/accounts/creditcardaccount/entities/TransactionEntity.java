package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities;

import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {

    private long postingDate;
    private long transactionDate;
    private String postingDateAsString;
    private String transactionDateAsString;
    private String description;
    private Double currencyAmount;
    private Double creditAmount;
    private Double debitAmount;
    private String currency;
    private int currencyExchangeRate;
    private String creditNumber;
    private String transactionItemType;
    private String cardOwner;
    private boolean reservation;

    public long getPostingDate() {
        return postingDate;
    }

    public long getTransactionDate() {
        return transactionDate;
    }

    public String getPostingDateAsString() {
        return postingDateAsString;
    }

    public String getTransactionDateAsString() {
        return transactionDateAsString;
    }

    public String getDescription() {
        return description;
    }

    public Double getCurrencyAmount() {
        return currencyAmount;
    }

    public Double getCreditAmount() {
        return creditAmount;
    }

    public Double getDebitAmount() {
        return debitAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getCurrencyExchangeRate() {
        return currencyExchangeRate;
    }

    public String getCreditNumber() {
        return creditNumber;
    }

    public String getTransactionItemType() {
        return transactionItemType;
    }

    public String getCardOwner() {
        return cardOwner;
    }

    public boolean isReservation() {
        return reservation;
    }

    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setAmount(Amount.inNOK(-selectAmount()))
                    .setDate(
                            ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(
                                    transactionDateAsString))
                    .setDescription(description)
                    .setPending(isReservation())
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Date parse failed", e);
        }
    }

    private Double selectAmount() {
        // creditAmount and debitAmount are mutually exclusive and we prefer them over
        // currencyAmount since
        // they represent the amount in the user's local currency. Using currencyAmount as a last
        // resort.
        if (creditAmount != null) {
            return creditAmount;
        } else if (debitAmount != null) {
            return debitAmount;
        } else if (currencyAmount != null) {
            return currencyAmount;
        }

        throw new IllegalStateException(
                "DNB - No valid amount value found, can't create transaction");
    }
}
