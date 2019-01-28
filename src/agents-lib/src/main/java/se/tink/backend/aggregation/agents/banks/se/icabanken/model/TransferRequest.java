package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.libraries.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {
    @JsonProperty("Amount")
    private double amount;
    @JsonProperty("Memo")
    private String memo;
    @JsonProperty("FromAccountId")
    private String fromAccountId;
    @JsonProperty("DueDate")
    private String dueDate;
    @JsonProperty("Reference")
    private String reference;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("RecipientId")
    private String recipientId;
    @JsonProperty("RecipientAccountNumber")
    private String recipientAccountNumber;
    @JsonProperty("RecipientType")
    private String recipientType;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(TransferType type) {
        switch (type) {
            case PAYMENT:
                this.type = "Payment";
                break;
            case BANK_TRANSFER:
                this.type = "Transfer";
                break;
            default:
                break;
        }
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public void setRecipientAccountNumber(String recipientAccountNumber) {
        this.recipientAccountNumber = recipientAccountNumber;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

}
