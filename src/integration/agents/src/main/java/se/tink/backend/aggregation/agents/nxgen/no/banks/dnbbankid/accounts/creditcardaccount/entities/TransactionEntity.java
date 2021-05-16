package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.entities;

import java.text.ParseException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {

    private String transactionDateAsString;
    private String description;
    private Double currencyAmount;
    private Double creditAmount;
    private Double debitAmount;
    private String transactionItemType;
    private boolean reservation;

    public String getTransactionItemType() {
        return transactionItemType;
    }

    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setAmount(
                            ExactCurrencyAmount.of(
                                    getTransactionAmount(), DnbConstants.DEFAULT_CURRENCY))
                    .setDate(
                            ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.parse(
                                    transactionDateAsString))
                    .setDescription(description)
                    .setPending(reservation)
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Date parse failed", e);
        }
    }

    private Double getTransactionAmount() {
        // creditAmount and debitAmount are mutually exclusive and we prefer them over
        // currencyAmount since
        // they represent the amount in the user's local currency. Using currencyAmount as a last
        // resort.
        if (creditAmount != null) {
            return -1 * creditAmount;
        } else if (debitAmount != null) {
            return -1 * debitAmount;
        } else if (currencyAmount != null) {
            return -1 * currencyAmount;
        }

        throw new IllegalStateException(
                "DNB - No valid amount value found, can't create transaction");
    }
}
