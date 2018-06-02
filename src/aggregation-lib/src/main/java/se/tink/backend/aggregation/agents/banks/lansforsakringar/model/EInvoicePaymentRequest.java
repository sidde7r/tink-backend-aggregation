package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoicePaymentRequest {
    private double amount;
    private long date;
    private String electronicInvoiceId;
    private String fromAccount;
    private String ocr;
    private String toAccount;

    public double getAmount() {
        return amount;
    }

    public long getDate() {
        return date;
    }

    public String getElectronicInvoiceId() {
        return electronicInvoiceId;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getOcr() {
        return ocr;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setElectronicInvoiceId(String electronicInvoiceId) {
        this.electronicInvoiceId = electronicInvoiceId;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
}
