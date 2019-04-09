package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";
    private String toBgPg;
    private double amount;
    private String reference;
    private boolean amountScanned;
    private boolean referenceScanned;
    private String electronicInvoiceId;
    private boolean recipientScanned;
    private String referenceType;
    private String fromAccount;
    private long paymentDate;

    public String getToBgPg() {
        return toBgPg;
    }

    public void setToBgPg(String toBgPg) {
        this.toBgPg = toBgPg;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isAmountScanned() {
        return amountScanned;
    }

    public void setAmountScanned(boolean amountScanned) {
        this.amountScanned = amountScanned;
    }

    public boolean isReferenceScanned() {
        return referenceScanned;
    }

    public void setReferenceScanned(boolean referenceScanned) {
        this.referenceScanned = referenceScanned;
    }

    public String getElectronicInvoiceId() {
        return electronicInvoiceId;
    }

    public void setElectronicInvoiceId(String electronicInvoiceId) {
        this.electronicInvoiceId = electronicInvoiceId;
    }

    public boolean isRecipientScanned() {
        return recipientScanned;
    }

    public void setRecipientScanned(boolean recipientScanned) {
        this.recipientScanned = recipientScanned;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(long paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String calculateHash() {
        return String.valueOf(
                java.util.Objects.hash(getAmountForHash(amount), toBgPg, reference, fromAccount));
    }

    private String getAmountForHash(double amount) {
        return new DecimalFormat(
                        FOUR_POINT_PRECISION_FORMAT_STRING,
                        DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }
}
