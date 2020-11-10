package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.text.ParseException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionEntity {
    private String transactionId;
    private String accountId;
    private String archiveId;
    private String reference;
    private String message;
    private String amount;
    private String currency;
    private String creditDebitIndicator;
    private BigDecimal accountBalance;
    private PersonEntity creditor;
    private PersonEntity debtor;
    private String bookingDateTime;
    private String valueDateTime;
    private String status;
    private String isoTransactionCode;
    private String opTransactionCode;

    @JsonProperty("_links")
    private AccountLinkEntity accountLinkEntity;

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

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public String getStatus() {
        return status;
    }

    public String getIsoTransactionCode() {
        return isoTransactionCode;
    }

    public String getOpTransactionCode() {
        return opTransactionCode;
    }

    public PersonEntity getCreditor() {
        return creditor;
    }

    public PersonEntity getDebtor() {
        return debtor;
    }

    public String getBookingDateTime() {
        return bookingDateTime;
    }

    public String getValueDateTime() {
        return valueDateTime;
    }

    private String getDescription() {
        if (creditor != null) {
            return creditor.getAccountName();
        } else if (debtor != null) {
            return debtor.getAccountName();
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
                    .setDate(ThreadSafeDateFormat.FORMATTER_SECONDS_T_T.parse(bookingDateTime))
                    .setAmount(ExactCurrencyAmount.of(amount, currency))
                    .build();
        } catch (ParseException e) {
            throw new IllegalStateException("Parsing error with date");
        }
    }
}
