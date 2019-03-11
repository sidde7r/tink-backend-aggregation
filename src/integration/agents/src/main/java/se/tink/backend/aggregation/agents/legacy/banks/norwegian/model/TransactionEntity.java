package se.tink.backend.aggregation.agents.banks.norwegian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private static final AggregationLogger log = new AggregationLogger(TransactionEntity.class);

    private long accountTransactionId;
    private long externalId;
    private double amount;
    private String transactionDate;
    private String transactionText;
    private String transactionTypeText;
    private String message;
    @JsonProperty("isBooked")
    private boolean booked;
    private String postingDate;
    private String valueDate;

    public long getAccountTransactionId() {
        return accountTransactionId;
    }

    public void setAccountTransactionId(long accountTransactionId) {
        this.accountTransactionId = accountTransactionId;
    }

    public long getExternalId() {
        return externalId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFormattedTransactionDate() {
        if (transactionDate.contains("+")) {
            return transactionDate.substring(0, transactionDate.indexOf("+"));
        }

        return transactionDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionText() {
        return transactionText;
    }

    public void setTransactionText(String transactionText) {
        this.transactionText = transactionText;
    }

    public String getTransactionTypeText() {
        return transactionTypeText;
    }

    public void setTransactionTypeText(String transactionTypeText) {
        this.transactionTypeText = transactionTypeText;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(String postingDate) {
        this.postingDate = postingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public Transaction toTransaction() {
        Transaction t = new Transaction();
        t.setId(String.valueOf(externalId));
        t.setAmount(getAmount());
        try {
            t.setDate(DateUtils
                    .flattenTime(ThreadSafeDateFormat.FORMATTER_SECONDS_T.parse(getFormattedTransactionDate())));
        } catch (ParseException e) {
            throw new IllegalStateException("Unable to parse date", e);
        }
        t.setDescription(Strings.nullToEmpty(getTransactionText()).trim());

        switch (getTransactionTypeText().toLowerCase()) {
        case "reserverat":
            t.setPending(true);
            break;
        case "kontantuttag":
            t.setType(TransactionTypes.WITHDRAWAL);
            break;
        case "köp":
            t.setType(TransactionTypes.CREDIT_CARD);
            break;
        default:
            // NOP
        }

        log.info(
                String.format(
                        "[transactionTypeText]: %s [hasMessage]: %s",
                        getTransactionTypeText(),
                        !Strings.isNullOrEmpty(getMessage())));

        return t;
    }

    // Recent payments are not listed in the billed transactions for current month. They're first listed in
    // pending transactions. This method returns true if the accountTransactionId is 0 and the transactionTypeText
    // is "betalning".
    @JsonIgnore
    public boolean isNotBilledPayment() {
        return accountTransactionId == 0 && "betalning".equalsIgnoreCase(transactionTypeText);
    }
}
