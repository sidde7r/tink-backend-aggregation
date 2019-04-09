package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEntity {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";
    private double amount;
    private long date;
    private long deductionDate;
    private String fromAccount;
    private boolean paymentPastDueDate;
    private String paymentType;
    private RecipientEntity recipient;
    private String reference;
    private String uniqueId;

    public double getAmount() {
        return amount;
    }

    public long getDate() {
        return date;
    }

    public long getDeductionDate() {
        return deductionDate;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public RecipientEntity getRecipient() {
        return recipient;
    }

    public String getReference() {
        return reference;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isPaymentPastDueDate() {
        return paymentPastDueDate;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setDeductionDate(long deductionDate) {
        this.deductionDate = deductionDate;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setPaymentPastDueDate(boolean paymentPastDueDate) {
        this.paymentPastDueDate = paymentPastDueDate;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setRecipient(RecipientEntity recipient) {
        this.recipient = recipient;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String calculateHash() {
        return String.valueOf(
                java.util.Objects.hash(
                        getAmountForHash(amount),
                        recipient.getGiroNumber(),
                        reference,
                        fromAccount));
    }

    private String getAmountForHash(double amount) {
        return new DecimalFormat(
                        FOUR_POINT_PRECISION_FORMAT_STRING,
                        DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }
}
