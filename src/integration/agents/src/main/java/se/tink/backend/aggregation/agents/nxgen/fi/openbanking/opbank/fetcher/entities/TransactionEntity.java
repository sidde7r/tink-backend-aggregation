package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {
    private String archiveId;
    private String message;
    private String reference;
    private String amount;
    private String currency;
    private String bookingDate;
    private String valueDate;
    private String paymentDate;
    private RecipientEntity recipient;
    private String proprietaryTransactionDescription;
    private PayerEntity payer;

    public String getArchiveId() {
        return archiveId;
    }

    public String getMessage() {
        return message;
    }

    public String getReference() {
        return reference;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public RecipientEntity getRecipient() {
        return recipient;
    }

    public String getProprietaryTransactionDescription() {
        return proprietaryTransactionDescription;
    }

    public PayerEntity getPayer() {
        return payer;
    }

    private String getDescription() {
        if (payer != null) {
            return payer.getName();
        } else if (recipient != null) {
            return recipient.getName();
        } else if (!Strings.isNullOrEmpty(message)) {
            return message;
        }
        return "";
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        try {
            return Transaction.builder()
                    .setDescription(getDescription())
                    .setDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(bookingDate))
                    .setAmount(ExactCurrencyAmount.of(amount, currency))
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Parsing error with date");
        }
    }
}
