package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.text.ParseException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {
    private static final AggregationLogger log = new AggregationLogger(TransactionEntity.class);
    private String bookingDate;
    private double amount;
    private String receiverOrPayerName;
    private String reference;
    private String message;
    private String transactionId;
    private String transactionType;

    public String getBookingDate() {
        return bookingDate;
    }

    public double getAmount() {
        return amount;
    }

    public String getReceiverOrPayerName() {
        return receiverOrPayerName;
    }

    public String getReference() {
        return reference;
    }

    public String getMessage() {
        return message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Transaction toTinkTransaction() {
        Transaction.Builder transactionBuilder = Transaction.builder()
                .setDescription(getDescription())
                .setAmount(Amount.inEUR(amount));

        try {
            transactionBuilder.setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(bookingDate));
        } catch (ParseException e) {
            log.warn("Could not parse date for Aktia Bank", e);
        }

        return transactionBuilder.build();
    }

    @JsonIgnore
    private String getDescription() {
        if (Strings.isNullOrEmpty(message)) {
            return receiverOrPayerName;
        }

        return message;
    }
}
